package icu.windea.pls.config.util

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

object CwtLocationExpressionManager {
    fun resolvePlaceholder(locationExpression: CwtLocalisationLocationExpression, name: String): String {
        val r = buildString { for (c in locationExpression.location) if (c == '$') append(name) else append(c) }
        return if (locationExpression.forceUpperCase) r.uppercase() else r
    }


    fun resolvePlaceholder(locationExpression: CwtImageLocationExpression, name: String): String? {
        val placeholder = locationExpression.placeholder
        if (placeholder == null) return null
        return buildString { for (c in placeholder) if (c == '$') append(name) else append(c) }
    }

    fun resolve(
        locationExpression: CwtLocalisationLocationExpression,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
    ): CwtLocalisationLocationExpression.ResolveResult? {
        val (location, isPlaceholder, namePaths) = locationExpression

        if (isPlaceholder) {
            val nameText = when {
                namePaths.isNullOrEmpty() -> definitionInfo.name
                else -> namePaths.firstNotNullOfOrNull { definition.findByPath(location, ParadoxScriptValue::class.java, conditional = true, inline = true)?.stringValue() }
            }
            if (nameText.isNullOrEmpty()) return null
            val name = resolvePlaceholder(locationExpression, definitionInfo.name)
            if (name.isEmpty()) return null
            val resolved = ParadoxLocalisationSearch.search(name, selector).find()
            return CwtLocalisationLocationExpression.ResolveResult(name, resolved)
        }

        val valueElement = definition.findByPath(location, ParadoxScriptValue::class.java, conditional = true, inline = true) ?: return null
        val config = ParadoxExpressionManager.getConfigs(valueElement, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
        if (config.expression.type !in CwtDataTypeGroups.LocalisationLocationResolved) {
            return CwtLocalisationLocationExpression.ResolveResult("", null, PlsBundle.message("dynamic"))
        }
        if (valueElement !is ParadoxScriptString) {
            return null
        }
        if (valueElement.text.isParameterized()) {
            return CwtLocalisationLocationExpression.ResolveResult("", null, PlsBundle.message("parameterized"))
        }
        if (config.expression.type == CwtDataTypes.InlineLocalisation && valueElement.text.isLeftQuoted()) {
            return CwtLocalisationLocationExpression.ResolveResult("", null, PlsBundle.message("inlined"))
        }
        val name = valueElement.stringValue
        val resolved = ParadoxLocalisationSearch.search(name, selector).find()
        return CwtLocalisationLocationExpression.ResolveResult(name, resolved)
    }

    fun resolveAll(
        locationExpression: CwtLocalisationLocationExpression,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
    ): CwtLocalisationLocationExpression.ResolveAllResult? {
        val (location, isPlaceholder, namePaths) = locationExpression

        if (isPlaceholder) {
            val nameText = when {
                namePaths.isNullOrEmpty() -> definitionInfo.name
                else -> namePaths.firstNotNullOfOrNull { definition.findByPath(location, ParadoxScriptValue::class.java, conditional = true, inline = true)?.stringValue() }
            }
            if (nameText.isNullOrEmpty()) return null
            val name = resolvePlaceholder(locationExpression, definitionInfo.name)
            if (name.isEmpty()) return null
            val resolved = ParadoxLocalisationSearch.search(name, selector).findAll()
            return CwtLocalisationLocationExpression.ResolveAllResult(name, resolved)
        }

        val valueElement = definition.findByPath(location, ParadoxScriptValue::class.java, conditional = true, inline = true) ?: return null
        val config = ParadoxExpressionManager.getConfigs(valueElement, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
        if (config.expression.type !in CwtDataTypeGroups.LocalisationLocationResolved) {
            return CwtLocalisationLocationExpression.ResolveAllResult("", emptySet(), PlsBundle.message("dynamic"))
        }
        if (valueElement !is ParadoxScriptString) {
            return null
        }
        if (valueElement.text.isParameterized()) {
            return CwtLocalisationLocationExpression.ResolveAllResult("", emptySet(), PlsBundle.message("parameterized"))
        }
        if (config.expression.type == CwtDataTypes.InlineLocalisation && valueElement.text.isLeftQuoted()) {
            return CwtLocalisationLocationExpression.ResolveAllResult("", emptySet(), PlsBundle.message("inlined"))
        }
        val name = valueElement.stringValue
        val resolved = ParadoxLocalisationSearch.search(name, selector).findAll()
        return CwtLocalisationLocationExpression.ResolveAllResult(name, resolved)
    }

    fun resolve(
        locationExpression: CwtImageLocationExpression,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        frameInfo: ImageFrameInfo? = null,
        toFile: Boolean = false
    ): CwtImageLocationExpression.ResolveResult? {
        val tSprite = ParadoxDefinitionTypes.Sprite

        val project = definitionInfo.project
        var newFrameInfo = frameInfo
        if (definitionInfo.type == tSprite) {
            newFrameInfo = newFrameInfo merge ParadoxSpriteManager.getFrameInfo(definition)
        }

        val placeholder = locationExpression.placeholder
        val path = locationExpression.path
        if (placeholder != null) {
            if (definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            if (placeholder.startsWith("GFX_")) {
                val spriteName = resolvePlaceholder(locationExpression, definitionInfo.name)!!
                if (!toFile) {
                    val selector = selector(project, definition).definition().contextSensitive()
                    val resolved = ParadoxDefinitionSearch.search(spriteName, tSprite, selector).find()
                    return CwtImageLocationExpression.ResolveResult(spriteName, resolved, newFrameInfo)
                }
                val selector = selector(project, definition).definition().contextSensitive()
                val resolved = ParadoxDefinitionSearch.search(spriteName, tSprite, selector).find()
                val resolvedDefinition = resolved ?: return null
                val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                if (primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                return withRecursionGuard {
                    withRecursionCheck("${resolvedDefinitionInfo.name}:${resolvedDefinitionInfo.type}") {
                        primaryImageConfigs.firstNotNullOfOrNull { primaryImageConfig ->
                            val primaryLocationExpression = primaryImageConfig.locationExpression
                            val r = resolve(primaryLocationExpression, resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, true)
                            r?.takeIf { it.element != null || it.message != null }
                        }
                    }
                }
            }
            //假定这里的filePath以.dds结尾
            val filePath = resolvePlaceholder(locationExpression, definitionInfo.name)!!
            val selector = selector(project, definition).file().contextSensitive()
            val file = ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFile(project)
            return CwtImageLocationExpression.ResolveResult(filePath, file, newFrameInfo)
        } else if (path != null) {
            val valueElement = definition.findByPath(path, ParadoxScriptValue::class.java, conditional = true, inline = true) ?: return null
            val config = ParadoxExpressionManager.getConfigs(valueElement, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if (config.expression.type !in CwtDataTypeGroups.ImageLocationResolved) {
                return CwtImageLocationExpression.ResolveResult("", null, null, PlsBundle.message("dynamic"))
            }
            if (valueElement !is ParadoxScriptString) {
                return null
            }
            if (valueElement.text.isParameterized()) {
                return CwtImageLocationExpression.ResolveResult("", null, null, PlsBundle.message("parameterized"))
            }
            val resolved = ParadoxExpressionManager.resolveScriptExpression(valueElement, null, config, config.expression, false)
            when {
                //由filePath解析为图片文件
                resolved is PsiFile && resolved.fileType == DdsFileType -> {
                    val filePath = resolved.fileInfo?.path?.path ?: return null
                    val selector = selector(project, definition).file().contextSensitive()
                    val file = ParadoxFilePathSearch.search(filePath, null, selector).find()
                        ?.toPsiFile(project)
                    return CwtImageLocationExpression.ResolveResult(filePath, file, newFrameInfo)
                }
                //由name解析为定义（如果不是sprite，就继续向下解析）
                resolved is ParadoxScriptDefinitionElement -> {
                    val resolvedDefinition = resolved
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    if (!toFile && resolvedDefinitionInfo.type == tSprite) {
                        val spriteName = resolvedDefinitionInfo.name
                        val selector = selector(project, definition).definition().contextSensitive()
                        val r = ParadoxDefinitionSearch.search(spriteName, tSprite, selector).find() ?: return null
                        return CwtImageLocationExpression.ResolveResult(spriteName, r, newFrameInfo)
                    }
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if (primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                    return withRecursionGuard {
                        withRecursionCheck("${resolvedDefinitionInfo.name}:${resolvedDefinitionInfo.type}") {
                            primaryImageConfigs.firstNotNullOfOrNull { primaryImageConfig ->
                                val primaryLocationExpression = primaryImageConfig.locationExpression
                                val r = resolve(primaryLocationExpression, resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, toFile)
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

    fun resolveAll(
        locationExpression: CwtImageLocationExpression,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        frameInfo: ImageFrameInfo? = null,
        toFile: Boolean = false
    ): CwtImageLocationExpression.ResolveAllResult? {
        val tSprite = ParadoxDefinitionTypes.Sprite

        val project = definitionInfo.project
        var newFrameInfo = frameInfo
        if (definitionInfo.type == tSprite) {
            newFrameInfo = newFrameInfo merge ParadoxSpriteManager.getFrameInfo(definition)
        }

        val placeholder = locationExpression.placeholder
        val path = locationExpression.path
        if (placeholder != null) {
            if (definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            if (placeholder.startsWith("GFX_")) {
                val spriteName = resolvePlaceholder(locationExpression, definitionInfo.name)!!
                if (!toFile) {
                    val selector = selector(project, definition).definition().contextSensitive()
                    val resolved = ParadoxDefinitionSearch.search(spriteName, tSprite, selector).findAll()
                    return CwtImageLocationExpression.ResolveAllResult(spriteName, resolved, newFrameInfo)
                }
                val selector = selector(project, definition).definition().contextSensitive()
                val resolved = ParadoxDefinitionSearch.search(spriteName, tSprite, selector).find()
                val resolvedDefinition = resolved ?: return null
                val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                if (primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                return withRecursionGuard action@{
                    withRecursionCheck("${resolvedDefinitionInfo.name}:${resolvedDefinitionInfo.type}") {
                        var resolvedFilePath: String? = null
                        var resolvedElements: MutableSet<PsiElement>? = null
                        for (primaryImageConfig in primaryImageConfigs) {
                            val primaryLocationExpression = primaryImageConfig.locationExpression
                            val r = resolveAll(primaryLocationExpression, resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, true) ?: continue
                            if (r.message != null) return@action r
                            val (filePath, elements) = r
                            if (resolvedFilePath == null) resolvedFilePath = filePath
                            if (resolvedElements == null) resolvedElements = mutableSetOf()
                            resolvedElements.addAll(elements)
                        }
                        if (resolvedFilePath == null) return@action null
                        CwtImageLocationExpression.ResolveAllResult(resolvedFilePath, resolvedElements ?: emptySet(), newFrameInfo)
                    }
                }
            }
            //假定这里的filePath以.dds结尾
            val filePath = resolvePlaceholder(locationExpression, definitionInfo.name)!!
            val selector = selector(project, definition).file().contextSensitive()
            val files = ParadoxFilePathSearch.search(filePath, null, selector).findAll()
                .mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
            return CwtImageLocationExpression.ResolveAllResult(filePath, files, newFrameInfo)
        } else if (path != null) {
            val valueElement = definition.findByPath(path, ParadoxScriptValue::class.java, conditional = true, inline = true) ?: return null
            val config = ParadoxExpressionManager.getConfigs(valueElement, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if (config.expression.type !in CwtDataTypeGroups.ImageLocationResolved) {
                return CwtImageLocationExpression.ResolveAllResult("", emptySet(), null, PlsBundle.message("dynamic"))
            }
            if (valueElement !is ParadoxScriptString) {
                return null
            }
            if (valueElement.text.isParameterized()) {
                return CwtImageLocationExpression.ResolveAllResult("", emptySet(), null, PlsBundle.message("parameterized"))
            }
            val resolved = ParadoxExpressionManager.resolveScriptExpression(valueElement, null, config, config.expression, false)
            when {
                //由filePath解析为图片文件
                resolved is PsiFile && resolved.fileType == DdsFileType -> {
                    val filePath = resolved.fileInfo?.path?.path ?: return null
                    val selector = selector(project, definition).file().contextSensitive()
                    val files = ParadoxFilePathSearch.search(filePath, null, selector).findAll()
                        .mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
                    return CwtImageLocationExpression.ResolveAllResult(filePath, files, newFrameInfo)
                }
                //由name解析为定义（如果不是sprite，就继续向下解析）
                resolved is ParadoxScriptDefinitionElement -> {
                    val resolvedDefinition = resolved
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    if (!toFile && resolvedDefinitionInfo.type == tSprite) {
                        val spriteName = resolvedDefinitionInfo.name
                        val selector = selector(project, definition).definition().contextSensitive()
                        val r = ParadoxDefinitionSearch.search(spriteName, tSprite, selector).findAll()
                        return CwtImageLocationExpression.ResolveAllResult(spriteName, r, newFrameInfo)
                    }
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if (primaryImageConfigs.isEmpty()) return null //没有或者CWT规则不完善
                    return withRecursionGuard action@{
                        withRecursionCheck("${resolvedDefinitionInfo.name}:${resolvedDefinitionInfo.type}") {
                            var resolvedFilePath: String? = null
                            var resolvedElements: MutableSet<PsiElement>? = null
                            for (primaryImageConfig in primaryImageConfigs) {
                                val primaryLocationExpression = primaryImageConfig.locationExpression
                                val r = resolveAll(primaryLocationExpression, resolvedDefinition, resolvedDefinitionInfo, newFrameInfo, toFile) ?: continue
                                if (r.message != null) return@action r
                                val (filePath, elements) = r
                                if (resolvedFilePath == null) resolvedFilePath = filePath
                                if (resolvedElements == null) resolvedElements = mutableSetOf()
                                resolvedElements!!.addAll(elements)
                            }
                            if (resolvedFilePath == null) return@action null
                            CwtImageLocationExpression.ResolveAllResult(resolvedFilePath!!, resolvedElements ?: emptySet(), newFrameInfo)
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
