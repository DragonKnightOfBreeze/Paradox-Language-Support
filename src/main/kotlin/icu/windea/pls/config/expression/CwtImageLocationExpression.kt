package icu.windea.pls.config.expression

import com.google.common.cache.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import kotlin.collections.mapNotNullTo

private val validValueTypes = arrayOf(
    CwtDataType.FilePath,
    CwtDataType.Icon,
    CwtDataType.Definition
)

/**
 * CWT图片的位置表达式。
 *
 * 用于推断定义的相关图片（relatedImage）的位置。
 *
 * 示例：`"gfx/interface/icons/modifiers/mod_$.dds"`, `"#icon"`, "#icon|#icon_frame"`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成定义的名字，，如果定义是匿名的，则忽略此表达式）。
 * @property propertyName 属性名（表达式文本以"#"开始时，为"#"之后和可能的"|"之前的子字符串，可以为空字符串）。
 * @property extraPropertyNames 额外的属性名（表达式文本以"#"开始且之后包含"|"时，为"|"之后的按","分割的子字符串）。
 */
class CwtImageLocationExpression(
    expressionString: String,
    val placeholder: String? = null,
    val propertyName: String? = null,
    val extraPropertyNames: List<String>? = null
) : AbstractExpression(expressionString), CwtExpression {
    companion object Resolver {
        val EmptyExpression = CwtImageLocationExpression("")
        
        val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtImageLocationExpression> { doResolve(it) } }
        
        fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
        
        private fun doResolve(expressionString: String) = when {
            expressionString.isEmpty() -> EmptyExpression
            expressionString.startsWith('#') -> {
                val pipeIndex = expressionString.indexOf('|', 1)
                if(pipeIndex == -1) {
                    val propertyName = expressionString.substring(1)
                    CwtImageLocationExpression(expressionString, null, propertyName)
                } else {
                    val propertyName = expressionString.substring(1, pipeIndex)
                    val extraPropertyNames = expressionString.substring(pipeIndex + 1)
                        .splitToSequence(',').mapTo(SmartList()) { it.drop(1) }
                    CwtImageLocationExpression(expressionString, null, propertyName, extraPropertyNames)
                }
            }
            else -> CwtImageLocationExpression(expressionString, expressionString, null, null)
        }
    }
    
    operator fun component1() = placeholder
    
    operator fun component2() = propertyName
    
    operator fun component3() = extraPropertyNames
    
    fun resolvePlaceholder(name: String): String? {
        if(placeholder == null) return null
        return buildString { for(c in placeholder) if(c == '$') append(name) else append(c) }
    }
    
    data class ResolveResult(
        val filePath: String,
        val file: PsiFile?,
        val frame: Int,
        val message: String? = null
    )
    
    fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, project: Project, frame: Int = 0): ResolveResult? {
        if(placeholder != null) {
            //如果定义是匿名的，则直接忽略
            if(definitionInfo.isAnonymous) return null
            
            //假定这里的filePath以.dds结尾
            val filePath = resolvePlaceholder(definitionInfo.name)!!
            val selector = fileSelector(project, definition).contextSensitive()
            val file = ParadoxFilePathSearch.search(filePath, null, selector).find()
                ?.toPsiFile<PsiFile>(project)
            return ResolveResult(filePath, file, frame)
        } else if(propertyName != null) {
            //propertyName可以为空字符串，这时直接查找定义的字符串类型的值（如果存在）
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxConfigHandler.getValueConfigs(propertyValue, orDefault = false).firstOrNull() ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveResult("", null, 0, PlsDocBundle.message("dynamic"))
            }
            val key = propertyValue.value
            if(definitionInfo.name.equals(key, true)) return null //防止出现SOF
            val frameToUse = when {
                frame != 0 -> frame
                extraPropertyNames.isNullOrEmpty() -> 0
                else -> extraPropertyNames.mapAndFirst { propertyName ->
                    definition.findProperty(propertyName, conditional = true, inline = true)?.propertyValue?.intValue() ?: 0
                } ?: 0
            }
            val resolved = ParadoxConfigHandler.resolveScriptExpression(propertyValue, null, config, config.expression, config.info.configGroup, false)
            when {
                //由filePath解析为DDS文件
                resolved is PsiFile && resolved.fileType == DdsFileType -> {
                    val filePath = resolved.fileInfo?.path?.path ?: return null
                    val selector = fileSelector(project, definition).contextSensitive()
                    val file = ParadoxFilePathSearch.search(filePath, null, selector).find()
                        ?.toPsiFile<PsiFile>(project)
                    return ResolveResult(filePath, file, frameToUse)
                }
                //由filePath解析为definition，这里也可能是sprite之外的definition
                resolved is ParadoxScriptDefinitionElement -> {
                    val resolvedProject = resolved.project
                    val resolvedDefinition = resolved
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                    return primaryImageConfigs.firstNotNullOfOrNull { primaryImageConfig ->
                        val locationExpression = primaryImageConfig.locationExpression
                        val r = locationExpression.resolve(resolvedDefinition, resolvedDefinitionInfo, resolvedProject, frameToUse)
                        r?.takeIf { it.file != null || it.message != null }
                    }
                }
                else -> return null //解析失败或不支持
            }
        } else {
            return null //不期望的结果
        }
    }
    
    data class ResolveAllResult(
        val filePath: String,
        val files: Set<PsiFile>,
        val frame: Int,
        val message: String? = null
    )
    
    fun resolveAll(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, project: Project, frame: Int = 0): ResolveAllResult? {
        if(placeholder != null) {
            //假定这里的filePath以.dds结尾
            val filePath = buildString { for(c in placeholder) if(c == '$') append(definitionInfo.name) else append(c) }
            val selector = fileSelector(project, definition).contextSensitive()
            val files = ParadoxFilePathSearch.search(filePath, null, selector).findAll()
                .mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
            return ResolveAllResult(filePath, files, frame)
        } else if(!propertyName.isNullOrEmpty()) {
            //dynamic -> returns ("", null, 0)
            val property = definition.findProperty(propertyName, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxConfigHandler.getValueConfigs(propertyValue, orDefault = false).firstOrNull() ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveAllResult("", emptySet(), 0, PlsDocBundle.message("dynamic"))
            }
            val key = propertyValue.value
            if(definitionInfo.name.equals(key, true)) return null //防止出现SOF
            val frameToUse = when {
                frame != 0 -> frame
                extraPropertyNames.isNullOrEmpty() -> 0
                else -> extraPropertyNames.mapAndFirst { propertyName ->
                    definition.findProperty(propertyName, inline = true)?.propertyValue?.intValue() ?: 0
                } ?: 0
            }
            val resolved = ParadoxConfigHandler.resolveScriptExpression(propertyValue, null, config, config.expression, config.info.configGroup, false)
            when {
                //由filePath解析为DDS文件
                resolved is PsiFile && resolved.fileType == DdsFileType -> {
                    val filePath = resolved.fileInfo?.path?.path ?: return null
                    val selector = fileSelector(project, definition).contextSensitive()
                    val files = ParadoxFilePathSearch.search(filePath, null, selector).findAll()
                        .mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
                    return ResolveAllResult(filePath, files, frameToUse)
                }
                //由filePath解析为definition，这里也可能是sprite之外的definition
                resolved is ParadoxScriptDefinitionElement -> {
                    val resolvedProject = resolved.project
                    val resolvedDefinition = resolved
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if(primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                    var resolvedFilePath: String? = null
                    var resolvedSet: MutableSet<PsiFile>? = null
                    for(primaryImageConfig in primaryImageConfigs) {
                        val locationExpression = primaryImageConfig.locationExpression
                        val r = locationExpression.resolveAll(resolvedDefinition, resolvedDefinitionInfo, resolvedProject, frameToUse) ?: continue
                        if(r.message != null) return r
                        val (filePath, files) = r
                        if(resolvedFilePath == null) resolvedFilePath = filePath
                        if(resolvedSet == null) resolvedSet = mutableSetOf()
                        resolvedSet.addAll(files)
                    }
                    if(resolvedFilePath == null) return null
                    return ResolveAllResult(resolvedFilePath, resolvedSet ?: emptySet(), frameToUse)
                }
                else -> return null //解析失败或不支持
            }
        } else {
            return null //不期望的结果
        }
    }
}
