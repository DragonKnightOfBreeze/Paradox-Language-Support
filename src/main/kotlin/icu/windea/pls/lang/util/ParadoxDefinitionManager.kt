package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.util.CwtLocationExpressionManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxScriptService
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

/**
 * 用于处理定义。
 *
 * @see ParadoxScriptDefinitionElement
 * @see ParadoxDefinitionInfo
 */
object ParadoxDefinitionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInfo by createKey<CachedValue<ParadoxDefinitionInfo>>(Keys)
        val cachedDefinitionPrimaryLocalisationKey by createKey<CachedValue<String>>(Keys)
        val cachedDefinitionPrimaryLocalisation by createKey<CachedValue<ParadoxLocalisationProperty>>(Keys)
        val cachedDefinitionPrimaryLocalisations by createKey<CachedValue<Set<ParadoxLocalisationProperty>>>(Keys)
        val cachedDefinitionPrimaryImage by createKey<CachedValue<PsiFile>>(Keys)
    }

    // get info & match methods

    fun getInfo(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        // type key must be valid
        if (getTypeKey(element).isNullOrEmpty()) return null
        // get from cache
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = runReadActionSmartly { doGetInfo(element, file) }
            val trackers = listOfNotNull(
                file,
                value?.let { v -> ParadoxDefinitionService.getModificationTracker(v) },
            )
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetInfo(element: ParadoxScriptDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        doGetInfoFromStub(element, file)?.let { return it }
        return doGetInfoFromPsi(element, file)
    }

    fun doGetInfoFromStub(element: ParadoxScriptDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val stub = getStub(element) ?: return null
        val name = stub.definitionName
        val type = stub.definitionType
        val gameType = stub.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 `project`
        val typeConfig = configGroup.types[type] ?: return null
        val subtypes = stub.definitionSubtypes
        val subtypeConfigs = subtypes?.mapNotNull { typeConfig.subtypes[it] }
        val typeKey = stub.typeKey
        val elementPath = stub.elementPath
        return ParadoxDefinitionInfo(element, typeConfig, name, subtypeConfigs, typeKey, elementPath, gameType)
    }

    private fun doGetInfoFromPsi(element: ParadoxScriptDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType // 这里还是基于 `fileInfo` 获取 `gameType`
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 `project`
        val path = fileInfo.path
        val elementPath = ParadoxScriptService.getElementPath(element, PlsInternalSettings.getInstance().maxDefinitionDepth) ?: return null
        if (elementPath.path.isParameterized()) return null // 忽略成员路径带参数的情况
        val typeKey = getTypeKey(element) ?: return null
        val typeKeyPrefix = if (element is ParadoxScriptProperty) lazy { ParadoxScriptService.getKeyPrefixes(element).firstOrNull() } else null
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfig(element, configGroup, path, elementPath, typeKey, typeKeyPrefix) ?: return null
        return ParadoxDefinitionInfo(element, typeConfig, null, null, typeKey, elementPath.normalize(), gameType)
    }

    fun getTypeKey(element: ParadoxScriptDefinitionElement): String? {
        if (element is ParadoxScriptFile) return element.name.substringBeforeLast('.')
        val typeKey = element.name
        if (!typeKey.isIdentifier('.', '-')) return null // 必须是一个合法的标识符（排除可能带参数的情况，但仍然兼容一些特殊字符）
        if (ParadoxInlineScriptManager.isMatched(typeKey, selectGameType(element))) return null // 排除是内联脚本用法的情况
        return typeKey
    }

    fun getName(element: ParadoxScriptDefinitionElement): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.definitionName }
        return element.definitionInfo?.name
    }

    fun getType(element: ParadoxScriptDefinitionElement): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.definitionType }
        return element.definitionInfo?.type
    }

    fun getSubtypes(element: ParadoxScriptDefinitionElement): List<String>? {
        // 定义的子类型可能需要通过访问索引获取，不能在索引时就获取
        return element.definitionInfo?.subtypes
    }

    fun getStub(element: ParadoxScriptDefinitionElement): ParadoxScriptPropertyStub.Definition? {
        return element.castOrNull<ParadoxScriptProperty>()?.greenStub?.castOrNull()
    }

    fun getLocalizedNames(element: ParadoxScriptDefinitionElement): Set<String> {
        val primaryLocalisations = getPrimaryLocalisations(element)
        return primaryLocalisations.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getPrimaryLocalisationKey(element: ParadoxScriptDefinitionElement): String? {
        return doGetPrimaryLocalisationKeyFromCache(element)
    }

    private fun doGetPrimaryLocalisationKeyFromCache(element: ParadoxScriptDefinitionElement): String? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisationKey) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisationKey(element)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, LocalisationFile)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetPrimaryLocalisationKey(element: ParadoxScriptDefinitionElement): String? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者CWT规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = CwtLocationExpressionManager.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val key = resolveResult?.name ?: continue
            return key
        }
        return null
    }

    fun getPrimaryLocalisation(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return doGetPrimaryLocalisationFromCache(element)
    }

    private fun doGetPrimaryLocalisationFromCache(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisation(element)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, LocalisationFile, PreferredLocale)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetPrimaryLocalisation(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者CWT规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = CwtLocationExpressionManager.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val localisation = resolveResult?.element ?: continue
            return localisation
        }
        return null
    }

    fun getPrimaryLocalisations(element: ParadoxScriptDefinitionElement): Set<ParadoxLocalisationProperty> {
        return doGetPrimaryLocalisationsFromCache(element)
    }

    private fun doGetPrimaryLocalisationsFromCache(element: ParadoxScriptDefinitionElement): Set<ParadoxLocalisationProperty> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisations) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisations(element)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, LocalisationFile, PreferredLocale)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetPrimaryLocalisations(element: ParadoxScriptDefinitionElement): Set<ParadoxLocalisationProperty> {
        val definitionInfo = element.definitionInfo ?: return emptySet()
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return emptySet() // 没有或者CWT规则不完善
        val result = mutableSetOf<ParadoxLocalisationProperty>()
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = CwtLocationExpressionManager.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val localisations = resolveResult?.elements ?: continue
            result.addAll(localisations)
        }
        return result
    }

    fun getPrimaryImage(element: ParadoxScriptDefinitionElement): PsiFile? {
        return doGetPrimaryImageFromCache(element)
    }

    private fun doGetPrimaryImageFromCache(element: ParadoxScriptDefinitionElement): PsiFile? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryImage) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryImage(element)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, ScriptFile)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetPrimaryImage(element: ParadoxScriptDefinitionElement): PsiFile? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return null // 没有或者CWT规则不完善
        for (primaryImage in primaryImages) {
            val resolved = CwtLocationExpressionManager.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            val file = resolved?.element?.castOrNull<PsiFile>()
            if (file == null) continue
            element.putUserData(PlsKeys.imageFrameInfo, resolved.frameInfo)
            return file
        }
        return null
    }
}
