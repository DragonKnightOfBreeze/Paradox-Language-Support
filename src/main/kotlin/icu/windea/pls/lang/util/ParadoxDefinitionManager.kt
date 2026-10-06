package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.buildImmutableList
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
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
        val cachedDefinitionInfo by registerKey<CachedValue<ParadoxDefinitionInfo>>(Keys)
        val cachedSubtypeConfigs by registerKey<CachedValue<List<CwtSubtypeConfig>>>(Keys)
        val cachedSubtypeConfigsDumb by registerKey<CachedValue<List<CwtSubtypeConfig>>>(Keys)
        val cachedDeclaration by registerKey<CachedValue<Any>>(Keys) // Any: CwtPropertyConfig | EMPTY_OBJECT
        val cachedDeclarationDumb by registerKey<CachedValue<Any>>(Keys) // Any: CwtPropertyConfig | EMPTY_OBJECT
        val cachedPrimaryLocalisationKey by registerKey<CachedValue<String>>(Keys)
        val cachedPrimaryLocalisation by registerKey<CachedValue<ParadoxLocalisationProperty>>(Keys)
        val cachedPrimaryLocalisations by registerKey<CachedValue<Set<ParadoxLocalisationProperty>>>(Keys)
        val cachedPrimaryImage by registerKey<CachedValue<PsiFile>>(Keys)
        val cachedPrimaryImages by registerKey<CachedValue<Set<PsiFile>>>(Keys)

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
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val file = element.containingFile
                val value = ParadoxDefinitionService.resolveInfo(element, file)
                val dependencies = getInfoDependencies(element, file, value)
                CachedValueProvider.Result.create(value, dependencies)
            }
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
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxDefinitionService.resolveSubtypeConfigs(definitionInfo, finalOptions).optimized()
                CachedValueProvider.Result.create(value, getSubtypeAwareDependencies(element, definitionInfo))
            }
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
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxDefinitionService.resolveDeclaration(definitionInfo, finalOptions) ?: EMPTY_OBJECT
                CachedValueProvider.Result.create(value, getSubtypeAwareDependencies(element, definitionInfo))
            }
        }.castOrNull()
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
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryLocalisationKey) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisationKey(it) }
                CachedValueProvider.Result.create(value, element, ChronicleModificationTrackers.LocalisationFile)
            }
        }
    }

    fun getPrimaryLocalisation(element: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisation(it) }
                CachedValueProvider.Result.create(value, element, PsiModificationTracker.MODIFICATION_COUNT, ChronicleModificationTrackers.PreferredLocale)
            }
        }
    }

    fun getPrimaryLocalisations(element: ParadoxDefinitionElement): Set<ParadoxLocalisationProperty> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryLocalisations) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisations(it) }.orEmpty()
                CachedValueProvider.Result.create(value, element, PsiModificationTracker.MODIFICATION_COUNT, ChronicleModificationTrackers.PreferredLocale)
            }
        }
    }

    fun getPrimaryImage(element: ParadoxDefinitionElement): PsiFile? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryImage) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryImage(it) }
                CachedValueProvider.Result.create(value, element, PsiModificationTracker.MODIFICATION_COUNT)
            }
        }
    }

    @Suppress("unused")
    fun getPrimaryImages(element: ParadoxDefinitionElement): Set<PsiFile> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryImages) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryImages(it) }
                CachedValueProvider.Result.create(value, element, PsiModificationTracker.MODIFICATION_COUNT)
            }
        }
    }

    /**
     * 得到 [element] 对应的定义的所有相关本地化。
     */
    fun getRelatedLocalisations(element: ParadoxDefinitionElement): List<ParadoxLocalisationProperty> {
        val definitionInfo = getInfo(element) ?: return emptyList()
        return ParadoxDefinitionService.resolveRelatedLocalisations(definitionInfo)
    }

    // endregion

    // region Presentable Items

    /**
     * 得到 [element] 对应的定义的展示名字。
     *
     * @see ParadoxLocalisationManager.getPresentableText
     */
    fun getPresentableName(element: ParadoxDefinitionElement): String? {
        val localisation = getPrimaryLocalisation(element)
        return ParadoxLocalisationManager.getPresentableText(localisation)
    }

    /**
     * 得到 [element] 对应的定义的所有展示名字。
     *
     * @see ParadoxLocalisationManager.getPresentableText
     */
    fun getPresentableNames(element: ParadoxDefinitionElement): List<String> {
        val localisations = getPrimaryLocalisations(element)
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

    //endregion
}
