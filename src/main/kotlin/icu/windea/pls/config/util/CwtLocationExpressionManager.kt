package icu.windea.pls.config.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withConstraint
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxSpriteManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxLocalisationIndexConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.stringValue

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
        selectorBuilder: ParadoxSearchSelector<ParadoxLocalisationProperty>.() -> Unit = {}
    ): CwtLocalisationLocationExpression.ResolveResult? {
        val (location, isPlaceholder, namePaths) = locationExpression
        val project = definitionInfo.project

        if (isPlaceholder) {
            val nameText = findValueByPaths(definition, namePaths) ?: definitionInfo.name
            if (nameText.isEmpty()) return null
            val name = resolvePlaceholder(locationExpression, nameText)
            if (name.isNullOrEmpty()) return null
            return createLocalisationResolveResult(name, definition, definitionInfo, project, selectorBuilder)
        }

        val valueElement = findValueElementByPath(definition, location) ?: return null
        val config = ParadoxConfigManager.getConfigs(valueElement).firstOrNull() as? CwtValueConfig ?: return null
        if (config.configExpression.type !in CwtDataTypeGroups.LocalisationLocationAware) {
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
        return createLocalisationResolveResult(name, definition, definitionInfo, project, selectorBuilder)
    }

    private fun createLocalisationResolveResult(message: String): CwtLocalisationLocationExpression.ResolveResult {
        return CwtLocalisationLocationExpression.ResolveResult("", message)
    }

    private fun createLocalisationResolveResult(
        name: String,
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        project: Project,
        selectorBuilder: ParadoxSearchSelector<ParadoxLocalisationProperty>.() -> Unit
    ): CwtLocalisationLocationExpression.ResolveResult {
        return CwtLocalisationLocationExpression.ResolveResult(name, null, {
            val constraint = getLocalisationConstraint(definitionInfo) // use constraint here to optimize search performance
            val selector = selector(project, definition).localisation().contextSensitive()
                .withConstraint(constraint)
                .apply(selectorBuilder)
            ParadoxLocalisationSearch.searchNormal(name, selector).find()
        }, {
            val selector = selector(project, definition).localisation().contextSensitive()
                .apply(selectorBuilder)
            ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
        })
    }

    private fun getLocalisationConstraint(definitionInfo: ParadoxDefinitionInfo): ParadoxLocalisationIndexConstraint? {
        return when (definitionInfo.type) {
            ParadoxDefinitionTypes.event -> ParadoxLocalisationIndexConstraint.Event
            ParadoxDefinitionTypes.technology -> ParadoxLocalisationIndexConstraint.Tech
            else -> null
        }
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

        val nextFrameInfo = when {
            definitionInfo.type == ParadoxDefinitionTypes.sprite -> ParadoxSpriteManager.getFrameInfo(definition)
            else -> findValueByPaths(definition, framePaths)?.toIntOrNull()?.let { ImageFrameInfo.of(it) }
        }
        val newFrameInfo = ImageFrameInfo.merge(frameInfo, nextFrameInfo)

        if (isPlaceholder) {
            val nameText = findValueByPaths(definition, namePaths) ?: definitionInfo.name
            if (nameText.isEmpty()) return null
            if (location.startsWith("GFX_")) {
                val spriteName = resolvePlaceholder(locationExpression, nameText)
                if (spriteName.isNullOrEmpty()) return null
                if (toFile) {
                    val definitionSelector = selector(project, definition).definition().contextSensitive()
                    val resolved = ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.sprite, definitionSelector).find()
                    val resolvedDefinition = resolved ?: return null
                    val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                    val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                    if (primaryImageConfigs.isEmpty()) return null // 没有或者CWT规则不完善
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

        val valueElement = findValueElementByPath(definition, location) ?: return null
        val config = ParadoxConfigManager.getConfigs(valueElement).firstOrNull() as? CwtValueConfig ?: return null
        if (config.configExpression.type !in CwtDataTypeGroups.ImageLocationAware) {
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
            // 由filePath解析为图片文件
            resolved is PsiFile && ParadoxImageManager.isImageFile(resolved) -> {
                val filePath = resolved.fileInfo?.path?.path ?: return null
                return createImageResolveResultByFilePath(filePath, newFrameInfo, definition, project)
            }
            // 由name解析为定义（如果不是sprite，就继续向下解析）
            resolved is ParadoxScriptDefinitionElement -> {
                val resolvedDefinition = resolved
                val resolvedDefinitionInfo = resolved.definitionInfo ?: return null
                if (!toFile && resolvedDefinitionInfo.type == ParadoxDefinitionTypes.sprite) {
                    val spriteName = resolvedDefinitionInfo.name
                    return createImageResolveResult(spriteName, newFrameInfo, definition, project)
                }
                val primaryImageConfigs = resolvedDefinitionInfo.primaryImages
                if (primaryImageConfigs.isEmpty()) return null // 没有或者CWT规则不完善
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
            else -> return null // 解析失败或不支持
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
            ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.sprite, selector).find()
        }, {
            val selector = selector(project, definition).definition().contextSensitive()
            ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.sprite, selector).findAll()
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

    private fun findValueElementByPath(definition: ParadoxScriptDefinitionElement, path: String): ParadoxScriptValue? {
        if (path.isEmpty()) return null
        val properties = selectScope { definition.properties(conditional = true, inline = true).ofPath(path).asProperty() }
        return properties.firstNotNullOfOrNull { it.propertyValue }
    }

    private fun findValueByPaths(definition: ParadoxScriptDefinitionElement, paths: Set<String>?): String? {
        if (paths.isNullOrEmpty()) return null
        val properties = selectScope { definition.properties(conditional = true, inline = true).ofPaths(paths).asProperty() }
        return properties.firstNotNullOfOrNull { it.propertyValue?.stringValue() }
    }
}
