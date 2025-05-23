package icu.windea.pls.config.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

object CwtLocationExpressionManager {
    fun resolvePlaceholder(locationExpression: CwtLocationExpression, name: String): String? {
        if (!locationExpression.isPlaceholder) return null
        val r = buildString { for (c in locationExpression.location) if (c == '$') append(name) else append(c) }
        return when {
            locationExpression is CwtLocalisationLocationExpression && locationExpression.forceUpperCase -> r.uppercase()
            else -> r
        }
    }

    fun resolve(
        locationExpression: CwtLocalisationLocationExpression,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        selectorBuilder: ChainedParadoxSelector<ParadoxLocalisationProperty>.() -> Unit = {}
    ): CwtLocalisationLocationExpression.ResolveResult? {
        val (location, isPlaceholder, namePaths) = locationExpression
        val project = definitionInfo.project

        if (isPlaceholder) {
            val nameText = findByPaths(definition, namePaths) ?: definitionInfo.name
            if (nameText.isEmpty()) return null
            val name = resolvePlaceholder(locationExpression, nameText)
            if (name.isNullOrEmpty()) return null
            return createLocalisationResolveResult(name, definition, project, selectorBuilder)
        }

        val valueElement = definition.findByPath(location, ParadoxScriptValue::class.java, conditional = true, inline = true) ?: return null
        val config = ParadoxExpressionManager.getConfigs(valueElement, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
        if (config.configExpression.type !in CwtDataTypeGroups.LocalisationLocationResolved) {
            return createLocalisationResolveResult(PlsBundle.message("dynamic"))
        }
        if (valueElement !is ParadoxScriptString) {
            return null
        }
        if (valueElement.text.isParameterized()) {
            return createLocalisationResolveResult(PlsBundle.message("parameterized"))
        }
        if (config.configExpression.type == CwtDataTypes.InlineLocalisation && valueElement.text.isLeftQuoted()) {
            return createLocalisationResolveResult(PlsBundle.message("inlined"))
        }
        val name = valueElement.stringValue
        return createLocalisationResolveResult(name, definition, project, selectorBuilder)
    }

    private fun createLocalisationResolveResult(message: String): CwtLocalisationLocationExpression.ResolveResult {
        return CwtLocalisationLocationExpression.ResolveResult("", message)
    }

    private fun createLocalisationResolveResult(
        name: String,
        definition: ParadoxScriptDefinitionElement,
        project: Project,
        selectorBuilder: ChainedParadoxSelector<ParadoxLocalisationProperty>.() -> Unit
    ): CwtLocalisationLocationExpression.ResolveResult {
        return CwtLocalisationLocationExpression.ResolveResult(name, null, {
            val selector = selector(project, definition).localisation().contextSensitive().apply(selectorBuilder)
            ParadoxLocalisationSearch.search(name, selector).find()
        }, {
            val selector = selector(project, definition).localisation().contextSensitive().apply(selectorBuilder)
            ParadoxLocalisationSearch.search(name, selector).findAll()
        })
    }

    fun resolve(
        locationExpression: CwtImageLocationExpression,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        frameInfo: ImageFrameInfo? = null,
        toFile: Boolean = false
    ): CwtImageLocationExpression.ResolveResult? {
        val (location, isPlaceholder, namePaths, framePaths) = locationExpression
        val project = definitionInfo.project

        var newFrameInfo = frameInfo
        if (definitionInfo.type == ParadoxDefinitionTypes.Sprite) {
            newFrameInfo = newFrameInfo merge ParadoxSpriteManager.getFrameInfo(definition)
        } else {
            val frameFromPath = findByPaths(definition, framePaths)?.toIntOrNull()
            if (frameFromPath != null) {
                val frameInfoFromPath = ImageFrameInfo.of(frameFromPath)
                newFrameInfo = newFrameInfo merge frameInfoFromPath
            }
        }

        if (isPlaceholder) {
            val nameText = findByPaths(definition, namePaths) ?: definitionInfo.name
            if (nameText.isEmpty()) return null
            if (location.startsWith("GFX_")) {
                val spriteName = resolvePlaceholder(locationExpression, nameText)
                if (spriteName.isNullOrEmpty()) return null
                if (toFile) {
                    val definitionSelector = selector(project, definition).definition().contextSensitive()
                    val resolved = ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.Sprite, definitionSelector).find()
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

                return createImageResolveResult(spriteName, newFrameInfo, definition, project)
            }

            val filePath = resolvePlaceholder(locationExpression, nameText)
            if (filePath.isNullOrEmpty()) return null
            return createImageResolveResultByFilePath(filePath, newFrameInfo, definition, project)
        }

        val valueElement = definition.findByPath(location, ParadoxScriptValue::class.java, conditional = true, inline = true) ?: return null
        val config = ParadoxExpressionManager.getConfigs(valueElement, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
        if (config.configExpression.type !in CwtDataTypeGroups.ImageLocationResolved) {
            return createImageResolveResult(PlsBundle.message("dynamic"))
        }
        if (valueElement !is ParadoxScriptString) {
            return null
        }
        if (valueElement.text.isParameterized()) {
            return createImageResolveResult(PlsBundle.message("parameterized"))
        }
        val resolved = ParadoxExpressionManager.resolveScriptExpression(valueElement, null, config, config.configExpression, false)
        when {
            //由filePath解析为图片文件
            resolved is PsiFile && ParadoxImageManager.isImageFile(resolved) -> {
                val filePath = resolved.fileInfo?.path?.path ?: return null
                return createImageResolveResultByFilePath(filePath, newFrameInfo, definition, project)
            }
            //由name解析为定义（如果不是sprite，就继续向下解析）
            resolved is ParadoxScriptDefinitionElement -> {
                val resolvedDefinition = resolved
                val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                if (!toFile && resolvedDefinitionInfo.type == ParadoxDefinitionTypes.Sprite) {
                    val spriteName = resolvedDefinitionInfo.name
                    return createImageResolveResult(spriteName, newFrameInfo, definition, project)
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
    }

    private fun createImageResolveResult(
        spriteName: String,
        frameInfo: ImageFrameInfo?,
        definition: ParadoxScriptDefinitionElement,
        project: Project
    ): CwtImageLocationExpression.ResolveResult {
        return CwtImageLocationExpression.ResolveResult(spriteName, frameInfo, null, {
            val selector = selector(project, definition).definition().contextSensitive()
            ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.Sprite, selector).find()
        }, {
            val selector = selector(project, definition).definition().contextSensitive()
            ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.Sprite, selector).findAll()
        })
    }

    private fun createImageResolveResultByFilePath(
        filePath: String,
        frameInfo: ImageFrameInfo?,
        definition: ParadoxScriptDefinitionElement,
        project: Project
    ): CwtImageLocationExpression.ResolveResult {
        return CwtImageLocationExpression.ResolveResult(filePath, frameInfo, null, {
            val selector = selector(project, definition).file().contextSensitive()
            ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFile(project)
        }, {
            val selector = selector(project, definition).file().contextSensitive()
            ParadoxFilePathSearch.search(filePath, null, selector).findAll().mapNotNullTo(mutableSetOf()) { it.toPsiFile(project) }
        })
    }

    private fun createImageResolveResult(message: String): CwtImageLocationExpression.ResolveResult {
        return CwtImageLocationExpression.ResolveResult("", null, message)
    }

    private fun findByPaths(definition: ParadoxScriptDefinitionElement, paths: Set<String>?): String? {
        if (paths.isNullOrEmpty()) return null
        return paths.firstNotNullOfOrNull { definition.findByPath(it, ParadoxScriptValue::class.java, conditional = true, inline = true)?.stringValue() }
    }
}
