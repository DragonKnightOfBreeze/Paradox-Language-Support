package icu.windea.pls.lang.util

import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.PsiModificationTracker
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.buildImmutableList
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getCachedValueOnDemand
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchOptionsService
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.paths.ParadoxMemberPath

@Optimized
object ParadoxDefinitionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInfo by registerKey<CachedValue<ParadoxDefinitionInfo?>>(this)
        val cachedSubtypeConfigs by registerKey<CachedValue<List<CwtSubtypeConfig>>>(this)
        val cachedSubtypeConfigsDumb by registerKey<CachedValue<List<CwtSubtypeConfig>>>(this)
        val cachedDeclaration by registerKey<CachedValue<CwtPropertyConfig?>>(this)
        val cachedDeclarationDumb by registerKey<CachedValue<CwtPropertyConfig?>>(this)
        val cachedPrimaryLocalisationKey by registerKey<CachedValue<String?>>(this)
        val cachedPrimaryLocalisations by registerKey<CachedValue<List<ParadoxLocalisationProperty>>>(this)
        val cachedRelatedLocalisations by registerKey<CachedValue<List<ParadoxLocalisationProperty>>>(this)
        val cachedPrimaryImages by registerKey<CachedValue<List<PsiFile>>>(this)
        val cachedRelatedImages by registerKey<CachedValue<List<PsiFile>>>(this)

        /** 用于标记图片的帧数信息以便后续进行切分。 */
        val imageFrameInfo by registerKey<ImageFrameInfo>(Keys)
    }

    fun getName(element: ParadoxDefinitionElement): String? {
        return getInfo(element)?.name
    }

    fun getType(element: ParadoxDefinitionElement): String? {
        return getInfo(element)?.type
    }

    fun getSubtypes(element: ParadoxDefinitionElement): List<String>? {
        return getInfo(element)?.subtypes
    }

    fun getInfo(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
        return getInfoFromCache(element)
    }

    private fun getInfoFromCache(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
        return getCachedValueOnDemand(element, Keys.cachedDefinitionInfo, ChronicleCapabilities.Cache.definition) {
            val file = element.containingFile
            val value = ParadoxDefinitionService.resolveInfo(element, file)
            CachedValueProvider.Result.create(value, getInfoDependencies(element, file, value))
        }
    }

    fun getSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        val candidates = definitionInfo.typeConfig.subtypes
        if (candidates.isEmpty()) return emptyList()
        return getSubtypeConfigsFromCache(definitionInfo, options)
    }

    private fun getSubtypeConfigsFromCache(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions?): List<CwtSubtypeConfig> {
        val element = definitionInfo.element ?: return emptyList()
        val isDumb = ParadoxMatchOptionsService.isDumb(options)
        val finalOptions = if (isDumb) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val cacheKey = if (isDumb) Keys.cachedSubtypeConfigsDumb else Keys.cachedSubtypeConfigs
        return getCachedValueOnDemand(element, cacheKey, ChronicleCapabilities.Cache.definition) {
            val value = ParadoxDefinitionService.resolveSubtypeConfigs(definitionInfo, finalOptions).optimized()
            CachedValueProvider.Result.create(value, getSubtypeAwareDependencies(element, definitionInfo))
        }
    }

    fun getDeclaration(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        return getDeclarationFromCache(definitionInfo, options)
    }

    private fun getDeclarationFromCache(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions?): CwtPropertyConfig? {
        val element = definitionInfo.element ?: return null
        val isDumb = ParadoxMatchOptionsService.isDumb(options)
        val finalOptions = if (isDumb) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val cacheKey = if (isDumb) Keys.cachedDeclarationDumb else Keys.cachedDeclaration
        return getCachedValueOnDemand(element, cacheKey, ChronicleCapabilities.Cache.definition) {
            val value = ParadoxDefinitionService.resolveDeclaration(definitionInfo, finalOptions)
            CachedValueProvider.Result.create(value, getSubtypeAwareDependencies(element, definitionInfo))
        }
    }

    fun getMemberPath(definitionInfo: ParadoxDefinitionInfo): ParadoxMemberPath {
        // NOTE 2.1.2 file definition has empty member path
        if (definitionInfo.source == ParadoxDefinitionSource.File) return ParadoxMemberPath.resolveEmpty()
        // 3.0.1 optimize: build immutable list here
        // 3.0.1 optimize: construct sized array directly for better performance and memory
        val rootKeys = definitionInfo.rootKeys
        val size = rootKeys.size
        val subPaths = buildImmutableList(size + 1) {
            if (it != size) rootKeys[it] else definitionInfo.typeKey
        }
        return ParadoxMemberPath.resolve(subPaths)
    }

    fun getRelatedLocalisationInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedLocalisationInfo> {
        return ParadoxDefinitionService.resolveRelatedLocalisationInfos(definitionInfo).optimized()
    }

    fun getRelatedImageInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        return ParadoxDefinitionService.resolveRelatedImageInfos(definitionInfo).optimized()
    }

    fun getModifierInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.ModifierInfo> {
        return ParadoxDefinitionService.resolveModifierInfos(definitionInfo).optimized()
    }

    fun getPrimaryRelatedLocalisationInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedLocalisationInfo> {
        return ParadoxDefinitionService.resolveRelatedLocalisationInfos(definitionInfo).filterFast { it.isPrimaryKey() }.optimized()
    }

    fun getPrimaryRelatedImageInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        return ParadoxDefinitionService.resolveRelatedImageInfos(definitionInfo).filterFast { it.isPrimaryKey() }.optimized()
    }

    // region Related Items

    fun getPrimaryLocalisationKey(element: ParadoxDefinitionElement): String? {
        return getCachedValueOnDemand(element, Keys.cachedPrimaryLocalisationKey, ChronicleCapabilities.Cache.relatedItems) {
            val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisationKey(it) }
            CachedValueProvider.Result.create(value, element, ChronicleModificationTrackers.LocalisationFile)
        }
    }

    /**
     * 得到 [element] 对应的定义的所有相关本地化。
     *
     * 备注：[onlyPrimary] 为 `true` 时，仅解析（关联键）作为主键的本地化。
     */
    fun getRelatedLocalisations(
        element: ParadoxDefinitionElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
        onlyPrimary: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        val definitionInfo = getInfo(element) ?: return emptyList()
        val cacheKey = if (onlyPrimary) Keys.cachedPrimaryLocalisations else Keys.cachedRelatedLocalisations
        // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
        // NOTE 3.0.4 仅在（默认的）偏好语言环境时经过缓存，从而避免缓存键不能区分语言环境
        val onDemand = ChronicleCapabilities.Cache.relatedItems && preferredLocale == ParadoxLocaleManager.getPreferredLocaleConfig()
        val value = getCachedValueOnDemand(element, cacheKey, onDemand) {
            val resolved = ParadoxDefinitionService.resolveRelatedLocalisations(definitionInfo, preferredLocale, onlyPrimary).optimized()
            CachedValueProvider.Result.create(resolved, element, PsiModificationTracker.MODIFICATION_COUNT, ChronicleModificationTrackers.PreferredLocale)
        }
        return if (onlyOne) value.firstOrNull().to.singletonListOrEmpty() else value
    }

    /**
     * 得到 [element] 对应的定义的所有相关图片。
     *
     * 备注：[onlyPrimary] 为 `true` 时，仅解析（关联键）作为主键的图片。
     */
    fun getRelatedImages(
        element: ParadoxDefinitionElement,
        onlyOne: Boolean = false,
        onlyPrimary: Boolean = false,
    ): List<PsiFile> {
        val definitionInfo = getInfo(element) ?: return emptyList()
        val cacheKey = if (onlyPrimary) Keys.cachedPrimaryImages else Keys.cachedRelatedImages
        // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
        val value = getCachedValueOnDemand(element, cacheKey, ChronicleCapabilities.Cache.relatedItems) {
            val resolved = ParadoxDefinitionService.resolveRelatedImages(definitionInfo, onlyPrimary).optimized()
            CachedValueProvider.Result.create(resolved, element, PsiModificationTracker.MODIFICATION_COUNT)
        }
        return if (onlyOne) value.firstOrNull().to.singletonListOrEmpty() else value
    }

    // endregion

    // region Presentable Items

    /**
     * 得到 [element] 对应的定义的展示名字。
     *
     * @see ParadoxLocalisationManager.getPresentableText
     */
    fun getPresentableNames(
        element: ParadoxDefinitionElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        onlyOne: Boolean = false,
    ): List<String> {
        val localisations = getRelatedLocalisations(element, preferredLocale, onlyOne, onlyPrimary = true)
        return ParadoxLocalisationManager.getPresentableText(localisations)
    }

    // endregion

    // region Dependencies

    @Suppress("UNUSED_PARAMETER")
    fun getInfoDependencies(element: ParadoxDefinitionElement, file: PsiFile, value: ParadoxDefinitionInfo?): List<Any> {
        // 3.0.1 使用更精确的依赖
        if (value == null) return listOf(file)
        val typeConfig = value.typeConfig

        // 如果存在 rootKey，则需要直接依赖文件
        if (typeConfig.skipRootKey.isNotEmpty()) return listOf(file)

        // 如果可能存在 typeKeyPrefix，则需要依赖父节点
        if (typeConfig.typeKeyPrefixConfig != null || typeConfig.name in typeConfig.configGroup.typeModel.typeKeyPrefixAware) return listOf(element.parent)

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
        return listOf(element.containingFile, ChronicleModificationTrackers.ScriptFile)
    }

    // endregion
}
