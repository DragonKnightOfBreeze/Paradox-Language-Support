package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

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
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.definitionName }
        return element.definitionInfo?.name
    }

    fun getType(element: ParadoxDefinitionElement): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.definitionType }
        return element.definitionInfo?.type
    }

    fun getSubtypes(element: ParadoxDefinitionElement): List<String>? {
        // 定义的子类型可能需要通过访问索引获取，不能在索引时就获取
        return element.definitionInfo?.subtypes
    }

    fun getInfo(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
        // type key must be valid
        if (getTypeKey(element).isNullOrEmpty()) return null
        // get from cache
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
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

    private fun doGetInfo(element: ParadoxDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        doGetInfoFromStub(element, file)?.let { return it }
        return doGetInfoFromPsi(element, file)
    }

    fun doGetInfoFromStub(element: ParadoxDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val stub = getStub(element) ?: return null
        val name = stub.definitionName
        val type = stub.definitionType
        val gameType = stub.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 `project`
        val typeConfig = configGroup.types[type] ?: return null
        val subtypes = stub.definitionSubtypes
        val subtypeConfigs = subtypes?.mapNotNull { typeConfig.subtypes[it] }
        val typeKey = stub.typeKey
        val rootKeys = stub.rootKeys
        return ParadoxDefinitionInfo(element, typeConfig, name, subtypeConfigs, typeKey, rootKeys.optimized())
    }

    private fun doGetInfoFromPsi(element: ParadoxDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType // 这里还是基于 `fileInfo` 获取 `gameType`
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

    fun getStub(element: ParadoxDefinitionElement): ParadoxScriptPropertyStub.Definition? {
        return element.castOrNull<ParadoxScriptProperty>()?.greenStub?.castOrNull()
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
