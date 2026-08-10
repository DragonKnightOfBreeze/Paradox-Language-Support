package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionInheritSupport
import icu.windea.pls.ep.resolve.definition.ParadoxDefinitionModifierProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtSubtypeConfigMatchContext
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.getModeFromExpression
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager.getTargetFromExpression
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager.Keys
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModificationTrackers
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.orSpecific
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

@Optimized
object ParadoxDefinitionService {
    /**
     * @see ParadoxDefinitionInheritSupport.getSuperDefinition
     */
    fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxDefinitionElement? {
        val gameType = definitionInfo.gameType
        val eps = ParadoxDefinitionInheritSupport.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getSuperDefinition(definitionInfo)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxDefinitionInheritSupport.processSubtypeConfigs
     */
    fun processSubtypeConfigsFromInherit(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: MutableList<CwtSubtypeConfig>): Boolean {
        val gameType = definitionInfo.gameType
        val eps = ParadoxDefinitionInheritSupport.EP_NAME.extensionList
        return eps.processFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f true // check game type first
            ep.processSubtypeConfigs(definitionInfo, subtypeConfigs)
        }
    }

    /**
     * @see ParadoxDefinitionModifierProvider.getModifierCategories
     */
    fun getModifierCategories(definitionInfo: ParadoxDefinitionInfo): Map<String, CwtModifierCategoryConfig>? {
        val gameType = definitionInfo.gameType
        val eps = ParadoxDefinitionModifierProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            ep.getModifierCategories(definitionInfo)?.let { return it }
        }
        return null
    }

    fun resolveInfo(element: ParadoxDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val fileInfo = file.fileInfo ?: return null
        resolveInfoFromInjection(element, file, fileInfo)?.let { return it }
        val gameType = fileInfo.gameType
        val path = fileInfo.path
        val source = resolveSource(element) ?: return null
        val typeKey = ParadoxMemberService.getTypeKey(element) ?: return null
        // 3.0.1 懒加载（通常可以先检查 typeKey） + 忽略 rootKeys 深度超出限制，或者带参数的情况
        val lazyRootKeys = lazy { ParadoxMemberService.getRootKeys(element, maxDepth = ChronicleCapacities.maxDefinitionDepth(), parameterAware = false) }
        // 3.0.1 懒加载（通常都是不必要的）
        val lazyTypeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(element) }
        val configGroup = ChronicleFacade.getConfigGroup(file.project, gameType)
        val matchContext = CwtTypeConfigMatchContext(configGroup, path, typeKey, lazyRootKeys, lazyTypeKeyPrefix)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfig(matchContext, element) ?: return null
        val name = resolveName(element, typeKey, typeConfig)
        val type = typeConfig.name.orNull() ?: return null
        val rootKeys = lazyRootKeys.value?.optimized() ?: return null
        return ParadoxDefinitionInfo(source, name, type, typeKey, rootKeys, typeConfig).also { it.element = element }
    }

    private fun resolveInfoFromInjection(element: ParadoxDefinitionElement, file: PsiFile, fileInfo: ParadoxFileInfo): ParadoxDefinitionInfo? {
        if (element !is ParadoxScriptProperty) return null
        val gameType = fileInfo.gameType
        val path = fileInfo.path
        val source = ParadoxDefinitionSource.Injection
        val expression = element.name
        if (!ParadoxDefinitionInjectionManager.isMatched(expression, gameType)) return null
        if (!ParadoxDefinitionInjectionManager.isAvailable(element)) return null
        if (expression.isParameterized()) return null // 忽略带参数的情况
        val configGroup = ChronicleFacade.getConfigGroup(file.project, gameType)
        val mode = getModeFromExpression(expression)
        if (mode.isNullOrEmpty()) return null
        if (!ParadoxDefinitionInjectionManager.isCreateMode(mode, configGroup)) return null
        val target = getTargetFromExpression(expression)
        if (target.isNullOrEmpty()) return null
        val matchContext = CwtTypeConfigMatchContext(configGroup, path)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(matchContext) ?: return null
        val name = target
        val type = typeConfig.name.orNull() ?: return null
        val typeKey = name
        return ParadoxDefinitionInfo(source, name, type, typeKey, emptyList(), typeConfig).also { it.element = element }
    }

    fun resolveSource(element: ParadoxDefinitionElement): ParadoxDefinitionSource? {
        return when (element) {
            is ParadoxScriptFile -> ParadoxDefinitionSource.File
            is ParadoxScriptProperty -> ParadoxDefinitionSource.Property
            else -> null // unexpected
        }
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
        val element = definitionInfo.element ?: return emptyList()
        val typeConfig = definitionInfo.typeConfig
        val subtypesConfig = typeConfig.subtypes
        val typeKey = definitionInfo.typeKey

        val result = mutableListOf<CwtSubtypeConfig>()
        val context = CwtSubtypeConfigMatchContext(typeConfig.configGroup, result, typeKey, options)
        for (subtypeConfig in subtypesConfig.values) {
            val matched = ParadoxConfigMatchService.matchesSubtype(context, element, subtypeConfig)
            if (matched) result += subtypeConfig
        }

        // NOTE 2.1.8 avoid relying on non-indexed file data (e.g., super definition) when indexing (through this may loss some information)
        if (ParadoxMatchService.isDumb(options)) return result

        // NOTE 2.1.8 may inherit certain subtypes from super definitions
        processSubtypeConfigsFromInherit(definitionInfo, result)
        // NOTE 2.1.8 it's necessary to distinct by name here since inherit subtypes may be duplicate
        return result.distinctBy { it.name }
    }

    fun resolveDeclaration(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val element = definitionInfo.element ?: return null
        val name = definitionInfo.name
        val type = definitionInfo.type
        val configGroup = definitionInfo.configGroup
        val declarationConfig = configGroup.declarations.get(type) ?: return null
        val subtypeConfigs = ParadoxDefinitionManager.getSubtypeConfigs(definitionInfo, options)
        val subtypes = ParadoxConfigManager.getSubtypes(subtypeConfigs)
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, configGroup, name, type, subtypes)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    fun resolveDeclaration(element: PsiElement, type: String, subtypes: List<String>? = null, configGroup: CwtConfigGroup): CwtPropertyConfig? {
        val declarationConfig = configGroup.declarations.get(type) ?: return null
        val declarationConfigContext = ParadoxConfigService.getDeclarationConfigContext(element, configGroup, null, type, subtypes)
        return declarationConfigContext?.getConfig(declarationConfig)
    }

    fun resolveRelatedLocalisationInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedLocalisationInfo> {
        val locationConfigs = definitionInfo.typeConfig.localisation?.getLocationConfigs(definitionInfo.subtypes)?.orNull() ?: return emptyList()
        val result = buildList(locationConfigs.size) {
            for (config in locationConfigs) {
                val locationExpression = CwtLocalisationLocationExpression.resolve(config.value)
                val info = ParadoxDefinitionInfo.RelatedLocalisationInfo(config.key, locationExpression, config.required, config.primary)
                this += info
            }
        }
        return result
    }

    fun resolveRelatedImageInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        val locationConfigs = definitionInfo.typeConfig.images?.getLocationConfigs(definitionInfo.subtypes)?.orNull() ?: return emptyList()
        val result = buildList(locationConfigs.size) {
            for (config in locationConfigs) {
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

    fun resolvePrimaryLocalisationKey(definitionInfo: ParadoxDefinitionInfo): String? {
        val element = definitionInfo.element ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        primaryLocalisations.forEachFast f@{ primaryLocalisation ->
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            if (resolveResult !is CwtLocalisationLocationResolveResult.Static) return@f
            return resolveResult.name
        }
        return null
    }

    fun resolvePrimaryLocalisation(definitionInfo: ParadoxDefinitionInfo): ParadoxLocalisationProperty? {
        val element = definitionInfo.element ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        primaryLocalisations.forEachFast f@{ primaryLocalisation ->
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            if (resolveResult !is CwtLocalisationLocationResolveResult.Static) return@f
            return resolveResult.element
        }
        return null
    }

    fun resolvePrimaryLocalisations(definitionInfo: ParadoxDefinitionInfo): Set<ParadoxLocalisationProperty> {
        val element = definitionInfo.element ?: return emptySet()
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return emptySet() // 没有或者规则不完善
        val result = mutableSetOf<ParadoxLocalisationProperty>()
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        primaryLocalisations.forEachFast f@{ primaryLocalisation ->
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            if (resolveResult !is CwtLocalisationLocationResolveResult.Static) return@f
            result.addAll(resolveResult.elements)
        }
        return result
    }

    fun resolvePrimaryImage(definitionInfo: ParadoxDefinitionInfo): PsiFile? {
        val element = definitionInfo.element ?: return null
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return null // 没有或者规则不完善
        primaryImages.forEachFast f@{ primaryImage ->
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            if (resolveResult !is CwtImageLocationResolveResult.Static) return@f
            val file = resolveResult.element?.castOrNull<PsiFile>() ?: return@f
            element.putUserData(Keys.imageFrameInfo, resolveResult.frameInfo)
            return file
        }
        return null
    }

    fun resolvePrimaryImages(definitionInfo: ParadoxDefinitionInfo): Set<PsiFile> {
        val element = definitionInfo.element ?: return emptySet()
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return emptySet() // 没有或者规则不完善
        val result = mutableSetOf<PsiFile>()
        primaryImages.forEachFast f@{ primaryImage ->
            val resolveResult = ParadoxConfigExpressionService.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            if (resolveResult !is CwtImageLocationResolveResult.Static) return@f
            val files = resolveResult.elements.filterIsInstance<PsiFile>()
            element.putUserData(Keys.imageFrameInfo, resolveResult.frameInfo)
            result.addAll(files)
        }
        return result
    }

    @Suppress("UNUSED_PARAMETER")
    fun getInfoDependencies(element: ParadoxDefinitionElement, file: PsiFile, value: ParadoxDefinitionInfo?): List<Any> {
        // 3.0.1 使用更精确的依赖
        if (value == null) return listOf(file)
        val typeConfig = value.typeConfig

        // 如果存在 rootKey，则需要直接依赖文件
        if (typeConfig.skipRootKey.isNotEmpty()) return listOf(file)

        // 如果可能存在 typeKeyPrefix，则需要依赖父节点
        if (typeConfig.typeKeyPrefixConfig != null || typeConfig.name in typeConfig.configGroup.typesModel.typeKeyPrefixAware) return listOf(element.parent)

        // 其余情况，直接依赖 element
        return listOf(element)
    }

    fun getSubtypeAwareDependencies(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): List<Any> {
        val subtypes = definitionInfo.typeConfig.subtypes

        // 如果无子类型候选项，则直接依赖 element
        if (subtypes.isEmpty()) return listOf(element)

        // 如果所有子类型候选项都不依赖声明结构，则直接依赖 element（快速匹配）
        val allFastMatch = subtypes.values.all { it.config.configs.isNullOrEmpty() }
        if (allFastMatch) return listOf(element)

        // 如果需要依赖声明结构，则需要依赖任何脚本文件
        return listOf(element.containingFile, ParadoxModificationTrackers.ScriptFile)
    }

    fun getRelatedLocalisationKeyAwareDependencies(element: ParadoxDefinitionElement): List<Any> {
        return listOf(element.containingFile, ParadoxModificationTrackers.LocalisationFile)
    }

    fun getRelatedLocalisationAwareDependencies(element: ParadoxDefinitionElement): List<Any> {
        return listOf(element.containingFile, ParadoxModificationTrackers.LocalisationFile, ParadoxModificationTrackers.PreferredLocale)
    }

    fun getRelatedImageAwareDependencies(element: ParadoxDefinitionElement): List<Any> {
        return listOf(element.containingFile, ParadoxModificationTrackers.ScriptFile)
    }
}
