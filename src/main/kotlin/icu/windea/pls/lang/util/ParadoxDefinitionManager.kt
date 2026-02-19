package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.RegistedKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile

@Suppress("unused")
object ParadoxDefinitionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInfo by registerKey<CachedValue<ParadoxDefinitionInfo>>(Keys)
        val cachedDefinitionSubtypeConfigs by registerKey<CachedValue<List<CwtSubtypeConfig>>>(Keys)
        val cachedDefinitionSubtypeConfigsDumb by registerKey<CachedValue<List<CwtSubtypeConfig>>>(Keys)
        val cachedDefinitionDeclaration by registerKey<CachedValue<Any>>(Keys) // Any: CwtPropertyConfig | EMPTY_OBJECT
        val cachedDefinitionDeclarationDumb by registerKey<CachedValue<Any>>(Keys) // Any: CwtPropertyConfig | EMPTY_OBJECT
        val cachedDefinitionPrimaryLocalisationKey by registerKey<CachedValue<String>>(Keys)
        val cachedDefinitionPrimaryLocalisation by registerKey<CachedValue<ParadoxLocalisationProperty>>(Keys)
        val cachedDefinitionPrimaryLocalisations by registerKey<CachedValue<Set<ParadoxLocalisationProperty>>>(Keys)
        val cachedDefinitionPrimaryImage by registerKey<CachedValue<PsiFile>>(Keys)
        val cachedDefinitionPrimaryImages by registerKey<CachedValue<Set<PsiFile>>>(Keys)

        /** 用于标记图片的帧数信息以便后续进行切分。 */
        val imageFrameInfo by registerKey<ImageFrameInfo>(Keys)
    }

    fun getTypeKey(element: ParadoxDefinitionElement): String? {
        if (element is ParadoxScriptFile) return element.name.substringBeforeLast('.')
        val typeKey = element.name
        if (!typeKey.isIdentifier(".-")) return null // 必须是一个合法的标识符（排除可能带参数的情况，但仍然兼容一些特殊字符）
        if (ParadoxInlineScriptManager.isMatched(typeKey, element)) return null // 排除是内联脚本用法的情况
        return typeKey
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
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = element.containingFile
                val value = ParadoxDefinitionService.resolveInfo(element, file)
                val dependencies = ParadoxDefinitionService.getDependencies(element, file)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        val candidates = definitionInfo.typeConfig.subtypes
        if (candidates.isEmpty()) return emptyList()
        val element = definitionInfo.element ?: return emptyList()
        val cacheKey = getSubtypeConfigsCacheKey(options)
        if (cacheKey == null) {
            // no cache
            return runReadActionSmartly {
                ParadoxDefinitionService.resolveSubtypeConfigs(definitionInfo, options).optimized()
            }
        }
        // from cache
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = ParadoxDefinitionService.resolveSubtypeConfigs(definitionInfo, null).optimized()
                val dependencies = ParadoxDefinitionService.getSubtypeAwareDependencies(element, definitionInfo)
                value.withDependencyItems(dependencies)
            }
        }
    }

    private fun getSubtypeConfigsCacheKey(options: ParadoxMatchOptions? = null): RegistedKey<CachedValue<List<CwtSubtypeConfig>>>? {
        return when (options) {
            null, ParadoxMatchOptions.DEFAULT -> Keys.cachedDefinitionSubtypeConfigs
            ParadoxMatchOptions.DUMB -> Keys.cachedDefinitionSubtypeConfigsDumb
            else -> null
        }
    }

    fun getDeclaration(definitionInfo: ParadoxDefinitionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val element = definitionInfo.element ?: return null
        val cacheKey = getDeclarationCacheKey(options)
        if (cacheKey == null) {
            // no cache
            return runReadActionSmartly {
                ParadoxDefinitionService.resolveDeclaration(definitionInfo, options)
            }
        }
        // from cache
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = ParadoxDefinitionService.resolveDeclaration(definitionInfo, null) ?: EMPTY_OBJECT
                val dependencies = ParadoxDefinitionService.getSubtypeAwareDependencies(element, definitionInfo)
                value.withDependencyItems(dependencies)
            }
        }.castOrNull()
    }

    private fun getDeclarationCacheKey(options: ParadoxMatchOptions? = null): RegistedKey<CachedValue<Any>>? {
        return when (options) {
            null, ParadoxMatchOptions.DEFAULT -> Keys.cachedDefinitionDeclaration
            ParadoxMatchOptions.DUMB -> Keys.cachedDefinitionDeclarationDumb
            else -> null
        }
    }

    fun getMemberPath(definitionInfo: ParadoxDefinitionInfo): ParadoxMemberPath {
        // NOTE 2.1.2 file definition has empty member path
        if (definitionInfo.typeConfig.typePerFile) return ParadoxMemberPath.resolveEmpty()
        return ParadoxMemberPath.resolve(definitionInfo.rootKeys + definitionInfo.typeKey).normalize()
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
        return definitionInfo.localisations.filter { it.primary || it.primaryByInference }.optimized()
    }

    fun getPrimaryRelatedImageInfos(definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionInfo.RelatedImageInfo> {
        return definitionInfo.images.filter { it.primary || it.primaryByInference }.optimized()
    }

    fun getLocalizedName(element: ParadoxDefinitionElement): String? {
        val primaryLocalisation = getPrimaryLocalisation(element)
        return primaryLocalisation?.let { ParadoxLocalisationManager.getLocalizedText(it) }
    }

    fun getLocalizedNames(element: ParadoxDefinitionElement): Set<String> {
        val primaryLocalisations = getPrimaryLocalisations(element)
        return primaryLocalisations.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getPrimaryLocalisationKey(element: ParadoxDefinitionElement): String? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisationKey) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisationKey(it) }
                val dependencies = ParadoxDefinitionService.getRelatedLocalisationKeyAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryLocalisation(element: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisation(it) }
                val dependencies = ParadoxDefinitionService.getRelatedLocalisationAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryLocalisations(element: ParadoxDefinitionElement): Set<ParadoxLocalisationProperty> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisations) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryLocalisations(it) }.orEmpty()
                val dependencies = ParadoxDefinitionService.getRelatedLocalisationAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryImage(element: ParadoxDefinitionElement): PsiFile? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryImage) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryImage(it) }
                val dependencies = ParadoxDefinitionService.getRelatedImageAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getPrimaryImages(element: ParadoxDefinitionElement): Set<PsiFile> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryImages) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val value = element.definitionInfo?.let { ParadoxDefinitionService.resolvePrimaryImages(it) }
                val dependencies = ParadoxDefinitionService.getRelatedImageAwareDependencies(element)
                value.withDependencyItems(dependencies)
            }
        }
    }
}
