package icu.windea.pls.config.configContext

import com.github.benmanes.caffeine.cache.Cache
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.jetbrains.rd.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.match.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.paths.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

/**
 * 规则上下文。
 *
 * 用于后续获取对应的上下文规则（即所有可能的规则）以及匹配的规则，从而提供各种高级语言功能。例如代码高亮、引用解析、代码补全。
 *
 * 可以获取规则上下文不意味着可以获取对应的上下文规则。
 *
 * @see CwtConfigContextProvider
 */
class CwtConfigContext(
    val element: ParadoxScriptMember, // use element directly here
    val fileInfo: ParadoxFileInfo?,
    val elementPath: ParadoxElementPath?,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    lateinit var provider: CwtConfigContextProvider

    fun getConfigs(matchOptions: Int = ParadoxMatchOptions.Default): List<CwtMemberConfig<*>> {
        val rootFile = selectRootFile(element) ?: return emptyList()
        val cache = configGroup.configsCache.value.get(rootFile)
        val cachedKey = doGetCacheKey(matchOptions) ?: return emptyList()
        val cached = withRecursionGuard {
            withRecursionCheck(cachedKey) action@{
                try {
                    PlsStates.dynamicContextConfigs.set(false)
                    // use lock-freeze ConcurrentMap.getOrPut to prevent IDE freezing problems
                    cache.asMap().getOrPut(cachedKey) {
                        doGetConfigs(matchOptions)?.optimized().orEmpty()
                    }
                } finally {
                    // use uncached result if result context configs are dynamic (e.g., based on script context)
                    if (PlsStates.dynamicContextConfigs.get() == true) cache.invalidate(cachedKey)
                    PlsStates.dynamicContextConfigs.remove()
                }
            }
        } ?: emptyList() // unexpected recursion, return empty list
        return cached
    }

    private fun doGetCacheKey(matchOptions: Int): String? {
        return provider.getCacheKey(this, matchOptions)
    }

    private fun doGetConfigs(matchOptions: Int): List<CwtMemberConfig<*>>? {
        return provider.getConfigs(this, matchOptions)
    }

    fun skipMissingExpressionCheck(): Boolean {
        return provider.skipMissingExpressionCheck(this)
    }

    fun skipTooManyExpressionCheck(): Boolean {
        return provider.skipTooManyExpressionCheck(this)
    }

    object Keys : KeyRegistry()
}

@Optimized
private val CwtConfigGroup.configsCache by createKey(CwtConfigContext.Keys) {
    createCachedValue(project) {
        // rootFile -> cacheKey -> configs
        // use soft values to optimize memory
        createNestedCache<VirtualFile, _, _, Cache<String, List<CwtMemberConfig<*>>>> {
            CacheBuilder().softValues().build<String, List<CwtMemberConfig<*>>>().cancelable()
        }.withDependencyItems(ParadoxModificationTrackers.Match)
    }
}
