package icu.windea.pls.config

import com.google.common.cache.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.path.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.ep.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
import icu.windea.pls.model.path.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * CWT规则上下文。
 *
 * 用于后续获取对应的所有可能的CWT规则以及匹配的CWT规则，从而提供高级语言功能。例如代码高亮、引用解析、代码补全。
 *
 * 可以获取CWT规则上下文不意味着可以获取对应的所有可能的CWT规则。
 *
 * @property fileInfo 所在文件的文件信息。
 * @property definitionInfo 所在定义的定义信息。
 *
 * @see CwtConfigContextProvider
 */
class CwtConfigContext(
    element: ParadoxScriptMemberElement,
    val fileInfo: ParadoxFileInfo?,
    val elementPath: ParadoxElementPath?,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    private val elementPointer = element.createPointer()
    val element get() = elementPointer.element
    
    fun getConfigs(matchOptions: Int = Options.Default): List<CwtMemberConfig<*>> {
        val rootFile = selectRootFile(element) ?: return emptyList()
        val cache = configGroup.configsCache.value.get(rootFile)
        val cachedKey = doGetCacheKey(matchOptions) ?: return emptyList()
        val cached = withRecursionGuard("icu.windea.pls.config.config.CwtConfigContext.getConfigs") {
            withCheckRecursion(cachedKey) {
                cache.get(cachedKey) {
                    doGetConfigs(matchOptions)
                }
            }
        } ?: emptyList() //unexpected recursion, return empty list
        //some configs cannot be cached (e.g. from overridden configs)
        if(PlsContext.overrideConfigStatus.get() == true) cache.invalidate(cachedKey)
        PlsContext.overrideConfigStatus.remove()
        return cached
    }
    
    private fun doGetCacheKey(matchOptions: Int): String? {
        return provider!!.getCacheKey(this, matchOptions)
    }
    
    private fun doGetConfigs(matchOptions: Int): List<CwtMemberConfig<*>> {
        return provider!!.getConfigs(this, matchOptions).orEmpty()
    }
    
    fun skipMissingExpressionCheck(): Boolean {
        return provider!!.skipMissingExpressionCheck(this)
    }
    
    fun skipTooManyExpressionCheck(): Boolean {
        return provider!!.skipTooManyExpressionCheck(this)
    }
    
    object Keys : KeyRegistry("CwtConfigContext")
}

//rootFile -> cacheKey -> configs
//use soft values to optimize memory
//depends on config group, indices and inference statuses
private val CwtConfigGroup.configsCache by createKeyDelegate(CwtConfigContext.Keys) {
    createCachedValue(project) {
        val trackerProvider = ParadoxModificationTrackerProvider.getInstance(project)
        val tracker1 = trackerProvider.ScriptFileTracker
        val tracker2 = trackerProvider.LocalisationFileTracker
        val tracker3 = ParadoxModificationTrackerProvider.ParameterConfigInferenceTracker
        val tracker4 = ParadoxModificationTrackerProvider.InlineScriptConfigInferenceTracker
        NestedCache<VirtualFile, _, _, _> { CacheBuilder.newBuilder().buildCache<String, List<CwtMemberConfig<*>>>() }
            .withDependencyItems(tracker1, tracker2, tracker3, tracker4)
    }
}

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by createKeyDelegate(CwtConfigContext.Keys)
var CwtConfigContext.elementPathFromRoot: ParadoxElementPath? by createKeyDelegate(CwtConfigContext.Keys)
var CwtConfigContext.provider: CwtConfigContextProvider? by createKeyDelegate(CwtConfigContext.Keys)

fun CwtConfigContext.isDefinition(): Boolean {
    return definitionInfo != null && elementPathFromRoot.let { it != null && it.isEmpty() }
}

fun CwtConfigContext.isRootOrMember(): Boolean {
    return elementPathFromRoot != null
}
