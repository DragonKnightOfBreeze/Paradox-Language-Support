package icu.windea.pls.config.configContext

import com.github.benmanes.caffeine.cache.Cache
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.jetbrains.rd.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
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
 * 规则上下文不一定存在对应的上下文规则。
 * 如果一个规则上下文开始存在对应的上下文规则，并且需要在子上下文中展开，则视作根上下文。
 *
 * @param memberPath 相对于所在文件的成员路径。
 * @param memberPathFromRoot 相对于根上下文的成员路径。
 *
 * @see CwtConfigContextProvider
 */
class CwtConfigContext(
    val element: ParadoxScriptMember, // use element directly here
    val memberPath: ParadoxMemberPath?,
    val memberPathFromRoot: ParadoxMemberPath?,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    lateinit var provider: CwtConfigContextProvider

    fun getConfigs(matchOptions: Int = ParadoxMatchOptions.Default): List<CwtMemberConfig<*>> {
        val rootFile = selectRootFile(element) ?: return emptyList()
        val cache = configGroup.configsCache.value.get(rootFile)
        val cachedKey = doGetCacheKey(matchOptions) ?: return emptyList()
        val cached = withRecursionGuard {
            withRecursionCheck(cachedKey) {
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
private val CwtConfigGroup.configsCache by registerKey(CwtConfigContext.Keys) {
    createCachedValue(project) {
        // rootFile -> cacheKey -> configs
        // use soft values to optimize memory
        createNestedCache<VirtualFile, _, _, Cache<String, List<CwtMemberConfig<*>>>> {
            CacheBuilder().softValues().build<String, List<CwtMemberConfig<*>>>().cancelable()
        }.withDependencyItems(ParadoxModificationTrackers.Match)
    }
}
