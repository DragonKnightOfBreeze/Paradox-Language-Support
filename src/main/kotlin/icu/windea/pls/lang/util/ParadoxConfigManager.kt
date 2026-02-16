package icu.windea.pls.lang.util

import com.google.common.collect.ImmutableList
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.SoftConcurrentHashMap
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOccurrenceService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.orDefault
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.script.psi.ParadoxScriptMember

object ParadoxConfigManager {
    object Keys : KeyRegistry() {
        val cachedConfigContext by registerKey<CachedValue<CwtConfigContext>>(Keys)
        val cachedConfigsCache by registerKey<CachedValue<MutableMap<String, List<CwtMemberConfig<*>>>>>(Keys)
        val cachedChildOccurrencesCache by registerKey<CachedValue<MutableMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>>>(Keys)
    }

    /**
     * 得到 [element] 对应的脚本成员的规则上下文。
     */
    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return null
        return getConfigContextFromCache(memberElement)
    }

    private fun getConfigContextFromCache(element: ParadoxScriptMember): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigContext) {
            ProgressManager.checkCanceled()
            val value = ParadoxConfigService.getConfigContext(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.Resolve)
        }
    }

    /**
     * 得到 [element] 对应的脚本成员的与之匹配的一组成员规则。
     */
    fun getConfigs(element: PsiElement, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        ProgressManager.checkCanceled()
        val cache = getConfigsCacheFromCache(memberElement)
        val cacheKey = options.orDefault().toHashString(forMatched = true).optimized() // optimized to optimize memory
        return cache.getOrPut(cacheKey) { ParadoxConfigService.getConfigs(memberElement, options).optimized() }
    }

    private fun getConfigsCacheFromCache(element: ParadoxScriptMember): MutableMap<String, List<CwtMemberConfig<*>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigsCache) {
            val value = doGetConfigsCache()
            value.withDependencyItems(element, ParadoxModificationTrackers.Resolve)
        }
    }

    @Optimized
    private fun doGetConfigsCache(): MutableMap<String, List<CwtMemberConfig<*>>> {
        // return ContainerUtil.createConcurrentSoftValueMap() // use concurrent soft value map to optimize memory
        return SoftConcurrentHashMap() // use soft referenced concurrent map to optimize more memory
    }

    /**
     * 得到 [element] 对应的脚本成员的作为值的子句中的子成员的出现次数信息。
     */
    fun getChildOccurrences(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, ParadoxMatchOccurrence> {
        if (configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if (childConfigs.isEmpty()) return emptyMap()
        ProgressManager.checkCanceled()
        val cache = getChildOccurrencesCacheFromCache(element)
        val cacheKey = CwtConfigManipulator.getIdentifierKey(childConfigs, "\u0000", 1).optimized() // optimized to optimize memory
        return cache.getOrPut(cacheKey) { ParadoxMatchOccurrenceService.getChildOccurrences(element, configs).optimized() }
    }

    private fun getChildOccurrencesCacheFromCache(element: ParadoxScriptMember): MutableMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedChildOccurrencesCache) {
            val value = doGetChildOccurrencesCache()
            value.withDependencyItems(element, ParadoxModificationTrackers.Resolve)
        }
    }

    @Optimized
    private fun doGetChildOccurrencesCache(): MutableMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>> {
        // return ContainerUtil.createConcurrentSoftValueMap() // use concurrent soft value map to optimize memory
        return SoftConcurrentHashMap() // use soft referenced concurrent map to optimize more memory
    }

    @Optimized
    fun getSubtypes(subtypeConfigs: List<CwtSubtypeConfig>): List<String> {
        if (subtypeConfigs.isEmpty()) return emptyList()
        return ImmutableList.builderWithExpectedSize<String>(subtypeConfigs.size)
            .apply { subtypeConfigs.forEachFast { add(it.name) } }
            .build()
    }

    @Optimized
    fun getTypes(type: String?, subtypeConfigs: List<CwtSubtypeConfig>): List<String> {
        if (type == null) return emptyList()
        if (subtypeConfigs.isEmpty()) return listOf(type)
        return ImmutableList.builderWithExpectedSize<String>(subtypeConfigs.size + 1)
            .apply { add(type) }
            .apply { subtypeConfigs.forEachFast { add(it.name) } }
            .build()
    }

    @Optimized
    fun getTypeText(type: String?, subtypeConfigs: List<CwtSubtypeConfig>): String {
        if (type == null) return ""
        if (subtypeConfigs.isEmpty()) return type
        return buildString {
            append(type)
            subtypeConfigs.forEachFast { append(", ").append(it.name) }
        }
    }
}
