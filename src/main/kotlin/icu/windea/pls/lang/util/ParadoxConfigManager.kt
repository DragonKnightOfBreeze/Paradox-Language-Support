package icu.windea.pls.lang.util

import com.google.common.collect.ImmutableList
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigKeyManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.SoftValue
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOccurrenceService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.toHashString
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.script.psi.ParadoxScriptMember
import java.util.concurrent.ConcurrentMap

object ParadoxConfigManager {
    object Keys : KeyRegistry() {
        val cachedConfigContext by registerKey<CachedValue<CwtConfigContext>>(Keys)
        val cachedConfigsCache by registerKey<CachedValue<SoftValue<ConcurrentMap<String, List<CwtMemberConfig<*>>>>>>(Keys)
        val cachedChildOccurrencesCache by registerKey<CachedValue<SoftValue<ConcurrentMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>>>>(Keys)
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
            value.withDependencyItems(element, ParadoxModificationTrackers.ConfigResolution)
        }
    }

    /**
     * 得到 [element] 对应的脚本成员的一组作为上下文的成员规则。如果当前位置不存在规则上下文，则返回空列表。
     */
    fun getContextConfigs(element: PsiElement, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        val configContext = getConfigContextFromCache(memberElement) ?: return emptyList()
        return configContext.getConfigs(options)
    }

    /**
     * 得到 [element] 对应的脚本成员的一组匹配的成员规则。
     */
    fun getConfigs(element: PsiElement, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        ProgressManager.checkCanceled()
        val cacheKey = options.toHashString().optimized() // optimized to optimize memory
        val cache = getConfigsCacheFromCache(memberElement).dereference()
        return cache.getOrPut(cacheKey) { ParadoxConfigService.getConfigs(memberElement, options).optimized() }
    }

    @Optimized
    private fun getConfigsCacheFromCache(element: ParadoxScriptMember): SoftValue<ConcurrentMap<String, List<CwtMemberConfig<*>>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigsCache) {
            // use soft referenced concurrent map to optimize more memory
            val value = SoftValue.ofConcurrentMap<String, List<CwtMemberConfig<*>>>()
            value.withDependencyItems(element, ParadoxModificationTrackers.ConfigResolution)
        }
    }

    /**
     * 得到 [element] 对应的脚本成员的作为值的子句中的子成员的出现次数信息。
     */
    fun getChildOccurrences(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, ParadoxMatchOccurrence> {
        if (configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if (childConfigs.isEmpty()) return emptyMap()
        ProgressManager.checkCanceled()
        val cacheKey = CwtConfigKeyManager.getIdentifierKey(childConfigs, "\u0000", 1).optimized() // optimized to optimize memory
        val cache = getChildOccurrencesCacheFromCache(element).dereference()
        return cache.getOrPut(cacheKey) { ParadoxMatchOccurrenceService.getChildOccurrences(element, configs).optimized() }
    }

    @Optimized
    private fun getChildOccurrencesCacheFromCache(element: ParadoxScriptMember): SoftValue<ConcurrentMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedChildOccurrencesCache) {
            // use soft referenced concurrent map to optimize more memory
            val value = SoftValue.ofConcurrentMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>()
            value.withDependencyItems(element, ParadoxModificationTrackers.ConfigResolution)
        }
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

    fun <C: CwtMemberConfig<*>> collectConfigWithOverridden(element: PsiElement, config: C, result: MutableList<C>) {
        val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(element, config)
        if (overriddenConfigs.isNotEmpty()) {
            result.addAll(overriddenConfigs)
        } else {
            result.add(config)
        }
    }

    fun checkExtendedConfig(element: ParadoxExpressionElement, config: CwtMemberConfig<*>): Boolean {
        val value = element.value
        val configGroup = config.configGroup
        val configExpression = config.configExpression
        if (configExpression.type in CwtDataTypeSets.DefinitionAware) {
            val definitionType = configExpression.value ?: return false
            val configs = configGroup.extendedDefinitions.findByPattern(value, element, configGroup).orEmpty()
            val config = configs.find { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionType) }
            if (config != null) return true
            if (definitionType == ParadoxDefinitionTypes.gameRule) {
                val config = configGroup.extendedGameRules.findByPattern(value, element, configGroup)
                if (config != null) return true
            }
            if (definitionType == ParadoxDefinitionTypes.onAction) {
                val config = configGroup.extendedOnActions.findByPattern(value, element, configGroup)
                if (config != null) return true
            }
        }
        return false
    }
}
