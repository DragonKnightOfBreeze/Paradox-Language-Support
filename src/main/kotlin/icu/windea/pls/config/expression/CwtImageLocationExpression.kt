package icu.windea.pls.config.expression

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.CwtImageLocationExpression.*
import icu.windea.pls.config.expression.CwtImageLocationExpression.ResolveResult
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * CWT图片位置表达式。
 *
 * 用于定位定义的相关图片。
 *
 * 如果包含占位符`$`，将其替换成定义的名字后，尝试得到对应路径的图片，否则尝试得到对应名字的属性的值对应的图片。
 *
 * 示例：`"gfx/interface/icons/modifiers/mod_$.dds"`, "GFX_$", `"icon"`, "icon|p1,p2"`
 *
 * @property placeholder 占位符文本。其中的`"$"`会在解析时被替换成定义的名字。
 * @property propertyName 属性名，用于获取图片的引用文本。
 * @property framePropertyNames 属性名，用于获取帧数。帧数用于后续切分图片。
 */
interface CwtImageLocationExpression : CwtExpression {
    val placeholder: String?
    val propertyName: String?
    val framePropertyNames: List<String>?
    
    fun resolvePlaceholder(name: String): String?
    
    fun resolve(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        frameInfo: FrameInfo? = null,
        toFile: Boolean = false
    ): ResolveResult?
    
    fun resolveAll(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        frameInfo: FrameInfo? = null,
        toFile: Boolean = false
    ): ResolveAllResult?
    
    data class ResolveResult(
        val nameOrFilePath: String,
        val element: PsiElement?,
        val frameInfo: FrameInfo? = null,
        val message: String? = null
    )
    
    data class ResolveAllResult(
        val nameOrFilePath: String,
        val elements: Set<PsiElement>,
        val frameInfo: FrameInfo? = null,
        val message: String? = null
    )
    
    companion object Resolver {
        val EmptyExpression: CwtImageLocationExpression = doResolveEmpty()
        
        fun resolve(expressionString: String): CwtImageLocationExpression = cache.get(expressionString)
    }
}

//Implementations (cached & interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtImageLocationExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtImageLocationExpressionImpl("", propertyName = "")

private fun doResolve(expressionString: String): CwtImageLocationExpression {
    return when {
        expressionString.isEmpty() -> CwtImageLocationExpression.EmptyExpression
        expressionString.contains('$') -> {
            val placeholder = expressionString
            CwtImageLocationExpressionImpl(expressionString, placeholder = placeholder)
        }
        else -> {
            val propertyName = expressionString.substringBefore('|').intern()
            val framePropertyNames = expressionString.substringAfter('|', "").orNull()
                ?.toCommaDelimitedStringList()
            CwtImageLocationExpressionImpl(expressionString, propertyName = propertyName, framePropertyNames = framePropertyNames)
        }
    }
}

private class CwtImageLocationExpressionImpl : CwtImageLocationExpression {
    override val expressionString: String
    override val placeholder: String?
    override val propertyName: String?
    override val framePropertyNames: List<String>?
    
    constructor(expressionString: String, placeholder: String? = null, propertyName: String? = null, framePropertyNames: List<String>? = null) {
        this.expressionString = expressionString.intern()
        this.placeholder = placeholder?.intern()
        this.propertyName = propertyName?.intern()
        this.framePropertyNames = framePropertyNames
    }
    
    override fun resolvePlaceholder(name: String): String? {
        if(placeholder == null) return null
        return buildString { for(c in placeholder) if(c == '$') append(name) else append(c) }
    }
    
    override fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, frameInfo: FrameInfo?, toFile: Boolean): ResolveResult? {
        val project = definitionInfo.project
        var newFrameInfo = frameInfo
        if(definitionInfo.type == "sprite") {
            newFrameInfo = newFrameInfo.merge(ParadoxSpriteHandler.getFrameInfo(definition))
        }
        if(placeholder != null) {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            if(placeholder.startsWith("GFX_")) {
                val spriteName = resolvePlaceholder(definitionInfo.name)!!
                if(!toFile) {
                    val selector = definitionSelector(project, definition).contextSensitive()
                    val resolved = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find()
                    return ResolveResult(spriteName, resolved, newFrameInfo)
                }
                val selector = definitionSelector(project, definition).contextSensitive()
                val resolved = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find()
                val resolvedDefinition = resolved ?: return null
                val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                return withRecursionGuard("icu.windea.pls.lang.cwt.expression.CwtImageLocationExpression.resolve") {
                    withCheckRecursion(resolvedDefinitionInfo.name + ":" + resolvedDefinitionInfo.type) {
                        primaryImageConfigs.firstNotNullOfOrNull { primaryImageConfig ->
                            val locationExpression = primaryImageConfig.locationExpression
                            val r = locationExpression.resolve(resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, true)
                            r?.takeIf { it.element != null || it.message != null }
                        }
                    }
                }
            }
            //假定这里的filePath以.dds结尾
            val filePath = resolvePlaceholder(definitionInfo.name)!!
            val selector = fileSelector(project, definition).contextSensitive()
            val file = ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFile(project)
            return ResolveResult(filePath, file, newFrameInfo)
        } else if(propertyName != null) {
            //propertyName可以为空字符串，这时直接查找定义的字符串类型的值（如果存在）
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = CwtConfigHandler.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if(config.expression.type !in CwtDataTypeGroups.ImageLocationResolved) {
                return ResolveResult("", null, null, PlsBundle.message("dynamic"))
            }
            if(propertyValue !is ParadoxScriptString) {
                return null
            }
            if(propertyValue.text.isParameterized()) {
                return ResolveResult("", null, null, PlsBundle.message("parameterized"))
            }
            val resolved = CwtConfigHandler.resolveScriptExpression(propertyValue, null, config, config.expression, config.info.configGroup, false)
            when {
                //由filePath解析为图片文件
                resolved is PsiFile && resolved.fileType == DdsFileType -> {
                    val filePath = resolved.fileInfo?.path?.path ?: return null
                    val selector = fileSelector(project, definition).contextSensitive()
                    val file = ParadoxFilePathSearch.search(filePath, null, selector).find()
                        ?.toPsiFile(project)
                    return ResolveResult(filePath, file, newFrameInfo)
                }
                //由name解析为定义（如果不是sprite，就继续向下解析）
                resolved is ParadoxScriptDefinitionElement -> {
                    val resolvedDefinition = resolved
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    if(!toFile && resolvedDefinitionInfo.type == "sprite") {
                        val spriteName = resolvedDefinitionInfo.name
                        val selector = definitionSelector(project, definition).contextSensitive()
                        val r = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
                        return ResolveResult(spriteName, r, newFrameInfo)
                    }
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                    return withRecursionGuard("icu.windea.pls.lang.cwt.expression.CwtImageLocationExpression.resolve") {
                        withCheckRecursion(resolvedDefinitionInfo.name + ":" + resolvedDefinitionInfo.type) {
                            primaryImageConfigs.firstNotNullOfOrNull { primaryImageConfig ->
                                val locationExpression = primaryImageConfig.locationExpression
                                val r = locationExpression.resolve(resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, toFile)
                                r?.takeIf { it.element != null || it.message != null }
                            }
                        }
                    }
                }
                else -> return null //解析失败或不支持
            }
        } else {
            throw IllegalStateException() //不期望的结果
        }
    }
    
    override fun resolveAll(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, frameInfo: FrameInfo?, toFile: Boolean): ResolveAllResult? {
        val project = definitionInfo.project
        var newFrameInfo = frameInfo
        if(definitionInfo.type == "sprite") {
            newFrameInfo = newFrameInfo.merge(ParadoxSpriteHandler.getFrameInfo(definition))
        }
        if(placeholder != null) {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            if(placeholder.startsWith("GFX_")) {
                val spriteName = resolvePlaceholder(definitionInfo.name)!!
                if(!toFile) {
                    val selector = definitionSelector(project, definition).contextSensitive()
                    val resolved = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).findAll()
                    return ResolveAllResult(spriteName, resolved, newFrameInfo)
                }
                val selector = definitionSelector(project, definition).contextSensitive()
                val resolved = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find()
                val resolvedDefinition = resolved ?: return null
                val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                return withRecursionGuard("icu.windea.pls.lang.cwt.expression.CwtImageLocationExpression.resolveAll") {
                    withCheckRecursion(resolvedDefinitionInfo.name + ":" + resolvedDefinitionInfo.type) {
                        var resolvedFilePath: String? = null
                        var resolvedElements: MutableSet<PsiElement>? = null
                        for(primaryImageConfig in primaryImageConfigs) {
                            val locationExpression = primaryImageConfig.locationExpression
                            val r = locationExpression.resolveAll(resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, true) ?: continue
                            if(r.message != null) return r
                            val (filePath, elements) = r
                            if(resolvedFilePath == null) resolvedFilePath = filePath
                            if(resolvedElements == null) resolvedElements = mutableSetOf()
                            resolvedElements.addAll(elements)
                        }
                        if(resolvedFilePath == null) return null
                        ResolveAllResult(resolvedFilePath, resolvedElements ?: emptySet(), newFrameInfo)
                    }
                }
            }
            //假定这里的filePath以.dds结尾
            val filePath = resolvePlaceholder(definitionInfo.name)!!
            val selector = fileSelector(project, definition).contextSensitive()
            val files = ParadoxFilePathSearch.search(filePath, null, selector).findAll()
                .mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
            return ResolveAllResult(filePath, files, newFrameInfo)
        } else if(propertyName != null) {
            //dynamic -> returns ("", null, 0)
            val property = definition.findProperty(propertyName, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = CwtConfigHandler.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if(config.expression.type !in CwtDataTypeGroups.ImageLocationResolved) {
                return ResolveAllResult("", emptySet(), null, PlsBundle.message("dynamic"))
            }
            if(propertyValue !is ParadoxScriptString) {
                return null
            }
            if(propertyValue.text.isParameterized()) {
                return ResolveAllResult("", emptySet(), null, PlsBundle.message("parameterized"))
            }
            val resolved = CwtConfigHandler.resolveScriptExpression(propertyValue, null, config, config.expression, config.info.configGroup, false)
            when {
                //由filePath解析为图片文件
                resolved is PsiFile && resolved.fileType == DdsFileType -> {
                    val filePath = resolved.fileInfo?.path?.path ?: return null
                    val selector = fileSelector(project, definition).contextSensitive()
                    val files = ParadoxFilePathSearch.search(filePath, null, selector).findAll()
                        .mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
                    return ResolveAllResult(filePath, files, newFrameInfo)
                }
                //由name解析为定义（如果不是sprite，就继续向下解析）
                resolved is ParadoxScriptDefinitionElement -> {
                    val resolvedDefinition = resolved
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    if(!toFile && resolvedDefinitionInfo.type == "sprite") {
                        val spriteName = resolvedDefinitionInfo.name
                        val selector = definitionSelector(project, definition).contextSensitive()
                        val r = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).findAll()
                        return ResolveAllResult(spriteName, r, newFrameInfo)
                    }
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                    return withRecursionGuard("icu.windea.pls.lang.cwt.expression.CwtImageLocationExpression.resolveAll") {
                        withCheckRecursion(resolvedDefinitionInfo.name + ":" + resolvedDefinitionInfo.type) {
                            var resolvedFilePath: String? = null
                            var resolvedElements: MutableSet<PsiElement>? = null
                            for(primaryImageConfig in primaryImageConfigs) {
                                val locationExpression = primaryImageConfig.locationExpression
                                val r = locationExpression.resolveAll(resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, toFile) ?: continue
                                if(r.message != null) return r
                                val (filePath, elements) = r
                                if(resolvedFilePath == null) resolvedFilePath = filePath
                                if(resolvedElements == null) resolvedElements = mutableSetOf()
                                resolvedElements!!.addAll(elements)
                            }
                            if(resolvedFilePath == null) return null
                            ResolveAllResult(resolvedFilePath!!, resolvedElements ?: emptySet(), newFrameInfo)
                        }
                    }
                }
                else -> return null //解析失败或不支持
            }
        } else {
            throw IllegalStateException() //不期望的结果
        }
    }
}
