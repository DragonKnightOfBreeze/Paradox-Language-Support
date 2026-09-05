package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
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
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
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
        // from cache
        return getInfoFromCache(element)
    }

    private fun getInfoFromCache(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val file = element.containingFile
                val value = ParadoxDefinitionService.resolveInfo(element, file)
                val dependencies = ParadoxDefinitionService.getInfoDependencies(element, file, value)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        val candidates = definitionInfo.typeConfig.subtypes
        if (candidates.isEmpty()) return emptyList()
        // from cache
        return getSubtypeConfigsFromCache(definitionInfo, options)
    }

    private fun getSubtypeConfigsFromCache(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions?): List<CwtSubtypeConfig> {
        val element = definitionInfo.element ?: return emptyList()
        val isDumb = ParadoxMatchService.isDumb(options)
        val finalOptions = if (isDumb) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val cacheKey = if (isDumb) Keys.cachedSubtypeConfigsDumb else Keys.cachedSubtypeConfigs
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxDefinitionService.resolveSubtypeConfigs(definitionInfo, finalOptions).optimized()
                val dependencies = ParadoxDefinitionService.getSubtypeAwareDependencies(element, definitionInfo)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getDeclaration(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        // from cache
        return getDeclarationFromCache(definitionInfo, options)
    }

    private fun getDeclarationFromCache(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions?): CwtPropertyConfig? {
        val element = definitionInfo.element ?: return null
        val isDumb = ParadoxMatchService.isDumb(options)
        val finalOptions = if (isDumb) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val cacheKey = if (isDumb) Keys.cachedDeclarationDumb else Keys.cachedDeclaration
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxDefinitionService.resolveDeclaration(definitionInfo, finalOptions) ?: EMPTY_OBJECT
                val dependencies = ParadoxDefinitionService.getSubtypeAwareDependencies(element, definitionInfo)
                value.withDependencyItems(dependencies)
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
        return definitionInfo.localisations.filterFast { it.isPrimaryKey() }.optimized()
    }

    fun getPrimaryRelatedImageInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        return definitionInfo.images.filterFast { it.isPrimaryKey() }.optimized()
    }

    fun getPresentableName(element: ParadoxDefinitionElement): String? {
        val primaryLocalisation = getPrimaryLocalisation(element)
        return primaryLocalisation?.let { ParadoxLocalisationManager.getPresentableText(it) }
    }

    fun getPresentableNames(element: ParadoxDefinitionElement): Set<String> {
        val primaryLocalisations = getPrimaryLocalisations(element)
        if (primaryLocalisations.isEmpty()) return emptySet()
        return primaryLocalisations.mapNotNull { ParadoxLocalisationManager.getPresentableText(it) }.toSet()
    }

    fun getPrimaryLocalisationKey(element: ParadoxDefinitionElement): String? {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryLocalisationKey) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisationKey(it) }
                val dependencies = ParadoxDefinitionService.getRelatedLocalisationKeyAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryLocalisation(element: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisation(it) }
                val dependencies = ParadoxDefinitionService.getRelatedLocalisationAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryLocalisations(element: ParadoxDefinitionElement): Set<ParadoxLocalisationProperty> {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryLocalisations) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisations(it) }.orEmpty()
                val dependencies = ParadoxDefinitionService.getRelatedLocalisationAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryImage(element: ParadoxDefinitionElement): PsiFile? {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryImage) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryImage(it) }
                val dependencies = ParadoxDefinitionService.getRelatedImageAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    @Suppress("unused")
    fun getPrimaryImages(element: ParadoxDefinitionElement): Set<PsiFile> {
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedPrimaryImages) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryImages(it) }
                val dependencies = ParadoxDefinitionService.getRelatedImageAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }
}
