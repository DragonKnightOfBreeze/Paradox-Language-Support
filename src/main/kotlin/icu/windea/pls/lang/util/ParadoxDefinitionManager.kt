package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 用于处理定义。
 *
 * @see ParadoxDefinitionElement
 * @see ParadoxDefinitionInfo
 */
@Suppress("unused")
object ParadoxDefinitionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInfo by registerKey<CachedValue<ParadoxDefinitionInfo>>(Keys)
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
        // type key must be valid
        if (getTypeKey(element).isNullOrEmpty()) return null
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = runReadActionSmartly { ParadoxDefinitionService.resolveInfo(element, file) }
            // TODO 2.1.3 定义信息本身只需要依赖 file 即可（子类型信息和声明信息可能有不同的依赖）
            val trackers = listOfNotNull(
                file,
                value?.let { v -> ParadoxDefinitionService.getModificationTracker(v) },
            )
            value.withDependencyItems(trackers)
        }
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
            val value = runReadActionSmartly r@{
                val definitionInfo = element.definitionInfo ?: return@r null
                ParadoxDefinitionService.resolvePrimaryLocalisationKey(definitionInfo, element)
            }
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, LocalisationFile)
            }
            value.withDependencyItems(trackers)
        }
    }

    fun getPrimaryLocalisation(element: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            val value = runReadActionSmartly r@{
                val definitionInfo = element.definitionInfo ?: return@r null
                ParadoxDefinitionService.resolvePrimaryLocalisation(definitionInfo, element)
            }
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, LocalisationFile, PreferredLocale)
            }
            value.withDependencyItems(trackers)
        }
    }

    fun getPrimaryLocalisations(element: ParadoxDefinitionElement): Set<ParadoxLocalisationProperty> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisations) {
            ProgressManager.checkCanceled()
            val value = runReadActionSmartly r@{
                val definitionInfo = element.definitionInfo ?: return@r null
                ParadoxDefinitionService.resolvePrimaryLocalisations(definitionInfo, element)
            }
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, LocalisationFile, PreferredLocale)
            }
            value.withDependencyItems(trackers)
        }
    }

    fun getPrimaryImage(element: ParadoxDefinitionElement): PsiFile? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryImage) {
            ProgressManager.checkCanceled()
            val value = runReadActionSmartly r@{
                val definitionInfo = element.definitionInfo ?: return@r null
                ParadoxDefinitionService.resolvePrimaryImage(definitionInfo, element)
            }
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, ScriptFile)
            }
            value.withDependencyItems(trackers)
        }
    }

    fun getPrimaryImages(element: ParadoxDefinitionElement): Set<PsiFile> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryImages) {
            ProgressManager.checkCanceled()
            val value = runReadActionSmartly r@{
                val definitionInfo = element.definitionInfo ?: return@r emptySet()
                ParadoxDefinitionService.resolvePrimaryImages(definitionInfo, element)
            }
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, ScriptFile)
            }
            value.withDependencyItems(trackers)
        }
    }
}
