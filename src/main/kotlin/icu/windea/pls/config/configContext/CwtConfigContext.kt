package icu.windea.pls.config.configContext

import com.google.common.cache.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.jetbrains.rd.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

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
    val element: ParadoxScriptMemberElement, //use element directly here
    val fileInfo: ParadoxFileInfo?,
    val elementPath: ParadoxExpressionPath?,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    fun getConfigs(matchOptions: Int = Options.Default): List<CwtMemberConfig<*>> {
        val rootFile = selectRootFile(element) ?: return emptyList()
        val cache = configGroup.configsCache.value.get(rootFile)
        val cachedKey = doGetCacheKey(matchOptions) ?: return emptyList()
        val cached = withRecursionGuard("CwtConfigContext.getConfigs") {
            withRecursionCheck(cachedKey) action@{
                try {
                    PlsManager.dynamicContextConfigs.set(false)
                    //use lock-freeze ConcurrentMap.getOrPut to prevent IDE freezing problems
                    cache.asMap().getOrPut(cachedKey) {
                        doGetConfigs(matchOptions)?.optimized().orEmpty()
                    }
                } finally {
                    //use uncached result if result context configs are dynamic (e.g., based on script context)
                    if (PlsManager.dynamicContextConfigs.get() == true) cache.invalidate(cachedKey)
                    PlsManager.dynamicContextConfigs.remove()
                }
            }
        } ?: emptyList() //unexpected recursion, return empty list
        return cached
    }

    private fun doGetCacheKey(matchOptions: Int): String? {
        return provider!!.getCacheKey(this, matchOptions)
    }

    private fun doGetConfigs(matchOptions: Int): List<CwtMemberConfig<*>>? {
        return provider!!.getConfigs(this, matchOptions)
    }

    fun skipMissingExpressionCheck(): Boolean {
        return provider!!.skipMissingExpressionCheck(this)
    }

    fun skipTooManyExpressionCheck(): Boolean {
        return provider!!.skipTooManyExpressionCheck(this)
    }

    object Keys : KeyRegistry()
}

//rootFile -> cacheKey -> configs
//use soft values to optimize memory
//depends on config group, indices and inference statuses
private val CwtConfigGroup.configsCache by createKey(CwtConfigContext.Keys) {
    createCachedValue(project) {
        val trackerProvider = ParadoxModificationTrackers
        createNestedCache<VirtualFile, _, _, _> {
            CacheBuilder.newBuilder().buildCache<String, List<CwtMemberConfig<*>>>()
        }.withDependencyItems(
            trackerProvider.ScriptFileTracker,
            trackerProvider.LocalisationFileTracker,
            ParadoxModificationTrackers.ParameterConfigInferenceTracker,
            ParadoxModificationTrackers.InlineScriptConfigInferenceTracker,
        )
    }
}

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.elementPathFromRoot: ParadoxExpressionPath? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.provider: CwtConfigContextProvider? by createKey(CwtConfigContext.Keys)

fun CwtConfigContext.isDefinition(): Boolean {
    return definitionInfo != null && elementPathFromRoot.let { it != null && it.isEmpty() }
}

fun CwtConfigContext.isRootOrMember(): Boolean {
    return elementPathFromRoot != null
}
