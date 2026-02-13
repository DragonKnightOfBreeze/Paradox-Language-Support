package icu.windea.pls.lang.resolve

import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.optimized
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionInheritSupport
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionModifierProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchOptionsUtil
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.ParadoxDefinitionManager.Keys
import icu.windea.pls.lang.util.ParadoxDefinitionManager.getTypeKey
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxDefinitionService {
    /**
     * @see ParadoxDefinitionInheritSupport.getSuperDefinition
     */
    fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxDefinitionElement? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionInheritSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getSuperDefinition(definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionInheritSupport.getModificationTracker
     */
    fun getModificationTrackerFromInherit(definitionInfo: ParadoxDefinitionInfo): ModificationTracker? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionInheritSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getModificationTracker(definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionInheritSupport.processSubtypeConfigs
     */
    fun processSubtypeConfigsFromInherit(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: MutableList<CwtSubtypeConfig>) {
        val gameType = definitionInfo.gameType
        ParadoxDefinitionInheritSupport.EP_NAME.extensionList.process p@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@p true
            ep.processSubtypeConfigs(definitionInfo, subtypeConfigs)
        }
    }

    /**
     * @see ParadoxDefinitionModifierProvider.getModifierCategories
     */
    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionModifierProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getModifierCategories(definitionInfo)
        }
    }

    fun resolveInfo(element: ParadoxDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path
        val maxDepth = PlsInternalSettings.getInstance().maxDefinitionDepth
        val typeKey = getTypeKey(element) ?: return null
        val rootKeys = ParadoxMemberService.getRootKeys(element, maxDepth = maxDepth) ?: return null
        if (rootKeys.any { it.isParameterized() }) return null // 忽略带参数的情况
        val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(element) }
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 `project`
        val matchContext = CwtTypeConfigMatchContext(configGroup, path, typeKey, rootKeys, typeKeyPrefix)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfig(matchContext, element) ?: return null
        return ParadoxDefinitionInfo(element, typeConfig, null, null, typeKey, rootKeys.optimized())
    }

    fun getModificationTracker(definitionInfo: ParadoxDefinitionInfo): ModificationTracker? {
        // NOTE 2.0.7 如果存在父定义，则可能需要依赖所有可声明父定义的脚本文件
        // TODO 2.0.7+ 完善了定义信息的解析逻辑后，这里可能还需要依赖内联脚本文件

        val fromInherit = getModificationTrackerFromInherit(definitionInfo)
        if (fromInherit != null) return fromInherit
        return null
    }

    fun resolveName(element: ParadoxDefinitionElement, typeKey: String, typeConfig: CwtTypeConfig): String {
        // NOTE 2.0.6 inline logic is not applied here
        // `name_from_file = yes` - use type key (aka file name without extension), remove prefix if exists (while the prefix is declared by config property `starts_with`)
        // no `name_field` - use type key (aka property name), remove prefix if exists (while the prefix is declared by config property `starts_with`)
        // `name_field = ""` - force empty (aka anonymous)
        // `name_field = "-"` - from property value (which should be a string)
        // else - from specific property value in definition declaration (while the property name is declared by config property `name_field`)
        return when {
            typeConfig.nameFromFile -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            typeConfig.nameField == null -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            else -> selectScope { element.nameFieldElement(typeConfig.nameField) }?.stringValue().orEmpty()
        }
    }

    fun resolveSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        val subtypesConfig = definitionInfo.typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (ParadoxConfigMatchService.matchesSubtype(definitionInfo.element, subtypeConfig, result, definitionInfo.typeKey, options)) {
                result += subtypeConfig
            }
        }
        // to avoid to rely on non-indexed file data (like super definition) when indexing (through this may loss some information)
        if (!ParadoxMatchOptionsUtil.skipIndex(options)) {
            processSubtypeConfigsFromInherit(definitionInfo, result)
        }
        return result.distinctBy { it.name } // it's necessary to distinct by name
    }

    fun resolveDeclaration(element: PsiElement, definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val configGroup = definitionInfo.configGroup
        val declarationConfig = configGroup.declarations.get(definitionInfo.type) ?: return null
        val subtypes = resolveSubtypeConfigs(definitionInfo, options).map { it.name }.optimized() // optimized to optimize memory
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, definitionInfo.name, definitionInfo.type, subtypes, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    fun resolveDeclaration(element: PsiElement, configGroup: CwtConfigGroup, type: String, subtypes: List<String>? = null): CwtPropertyConfig? {
        val declarationConfig = configGroup.declarations.get(type) ?: return null
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, null, type, subtypes, configGroup)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    fun resolveRelatedLocalisationInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedLocalisationInfo> {
        val mergedConfigs = definitionInfo.typeConfig.localisation?.getConfigs(definitionInfo.subtypes) ?: return emptyList()
        val result = buildList(mergedConfigs.size) {
            for (config in mergedConfigs) {
                val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
                val info = ParadoxDefinitionInfo.RelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
                this += info
            }
        }
        return result
    }

    fun resolveRelatedImageInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        val mergedConfigs = definitionInfo.typeConfig.images?.getConfigs(definitionInfo.subtypes) ?: return emptyList()
        val result = buildList(mergedConfigs.size) {
            for (config in mergedConfigs) {
                val locationExpression = CwtImageLocationExpression.resolve(config.value)
                val info = ParadoxDefinitionInfo.RelatedImageInfo(config.key, locationExpression, config.required, config.primary)
                this += info
            }
        }
        return result
    }

    fun resolveModifierInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.ModifierInfo> {
        val result = buildList {
            definitionInfo.configGroup.type2ModifiersMap.get(definitionInfo.type)?.forEach { (_, v) ->
                this += ParadoxDefinitionInfo.ModifierInfo(CwtConfigExpressionManager.extract(v.template, definitionInfo.name), v)
            }
            for (subtype in definitionInfo.subtypes) {
                definitionInfo.configGroup.type2ModifiersMap.get("${definitionInfo.type}.$subtype")?.forEach { (_, v) ->
                    this += ParadoxDefinitionInfo.ModifierInfo(CwtConfigExpressionManager.extract(v.template, definitionInfo.name), v)
                }
            }
        }
        return result
    }

    fun resolvePrimaryLocalisationKey(definitionInfo: ParadoxDefinitionInfo, element: ParadoxDefinitionElement): String? {
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val key = resolveResult?.name ?: continue
            return key
        }
        return null
    }

    fun resolvePrimaryLocalisation(definitionInfo: ParadoxDefinitionInfo, element: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val localisation = resolveResult?.element ?: continue
            return localisation
        }
        return null
    }

    fun resolvePrimaryLocalisations(definitionInfo: ParadoxDefinitionInfo, element: ParadoxDefinitionElement): Set<ParadoxLocalisationProperty> {
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return emptySet() // 没有或者规则不完善
        val result = mutableSetOf<ParadoxLocalisationProperty>()
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val localisations = resolveResult?.elements ?: continue
            result.addAll(localisations)
        }
        return result
    }

    fun resolvePrimaryImage(definitionInfo: ParadoxDefinitionInfo, element: ParadoxDefinitionElement): PsiFile? {
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return null // 没有或者规则不完善
        for (primaryImage in primaryImages) {
            val resolved = ParadoxConfigExpressionService.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            val file = resolved?.element?.castOrNull<PsiFile>()
            if (file == null) continue
            element.putUserData(Keys.imageFrameInfo, resolved.frameInfo)
            return file
        }
        return null
    }

    fun resolvePrimaryImages(definitionInfo: ParadoxDefinitionInfo, element: ParadoxDefinitionElement): Set<PsiFile> {
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return emptySet() // 没有或者规则不完善
        val result = mutableSetOf<PsiFile>()
        for (primaryImage in primaryImages) {
            val resolved = ParadoxConfigExpressionService.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            val files = resolved?.elements?.filterIsInstance<PsiFile>() ?: continue
            element.putUserData(Keys.imageFrameInfo, resolved.frameInfo)
            result.addAll(files)
        }
        return result
    }
}
