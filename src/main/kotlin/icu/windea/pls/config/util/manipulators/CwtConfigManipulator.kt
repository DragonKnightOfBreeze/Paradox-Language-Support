package icu.windea.pls.config.util.manipulators

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configExpression.CwtConfigExpressionService
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.isSamePointer
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.singleton
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.CwtType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object CwtConfigManipulator {
    object Keys : KeyRegistry() {
        val inBlockKeys by registerKey<Set<String>>(this)
    }

    // region Key Methods

    @Suppress("unused")
    @Optimized
    fun getIdentifierKey(config: CwtMemberConfig<*>, maxDepth: Int = -1): String {
        return doGetIdentifierKey(config, maxDepth)
    }

    @Optimized
    fun getIdentifierKey(configs: List<CwtMemberConfig<*>>, maxDepth: Int = -1): String {
        return doGetIdentifierKey(configs, maxDepth)
    }

    private fun doGetIdentifierKey(config: CwtMemberConfig<*>, maxDepth: Int, depth: Int = 0): String {
        if (maxDepth >= 0 && maxDepth < depth) return ""
        val children = config.configs
        return buildString {
            if (config is CwtPropertyConfig) append(config.key).append('=')
            when {
                children == null -> append(config.value)
                children.isEmpty() -> append("{}")
                else -> append('{').append(doGetIdentifierKey(children, maxDepth, depth + 1)).append('}')
            }
        }
    }

    private fun doGetIdentifierKey(configs: List<CwtMemberConfig<*>>, maxDepth: Int, depth: Int = 0): String {
        val size = configs.size
        return when (size) {
            0 -> ""
            1 -> doGetIdentifierKey(configs.get(0), maxDepth, depth)
            else -> configs.mapTo(FastList(size)) { doGetIdentifierKey(it, maxDepth, depth) }.sorted().joinToString("\u0000")
        }
    }

    @Suppress("unused")
    @Optimized
    fun getIdentifierKey(optionConfig: CwtOptionMemberConfig<*>): String {
        return doGetIdentifierKey(optionConfig)
    }

    @Suppress("unused")
    @Optimized
    fun getIdentifierKey(optionConfigs: List<CwtOptionMemberConfig<*>>): String {
        return doGetIdentifierKey(optionConfigs)
    }

    private fun doGetIdentifierKey(config: CwtOptionMemberConfig<*>): String {
        val children = config.optionConfigs
        return buildString {
            if (config is CwtOptionConfig) append(config.key).append('=')
            when {
                children == null -> append(config.value)
                children.isEmpty() -> append("{}")
                else -> append('{').append(doGetIdentifierKey(children)).append('}')
            }
        }
    }

    private fun doGetIdentifierKey(optionConfigs: List<CwtOptionMemberConfig<*>>): String {
        val size = optionConfigs.size
        return when (size) {
            0 -> ""
            1 -> doGetIdentifierKey(optionConfigs.get(0))
            else -> optionConfigs.mapTo(FastList(size)) { doGetIdentifierKey(it) }.sorted().joinToString("\u0000")
        }
    }

    fun getDistinctKey(config: CwtMemberConfig<*>): String {
        return doGetDistinctKey(config)
    }

    private fun doGetDistinctKey(config: CwtMemberConfig<*>, guardStack: MutableSet<String>? = null): String {
        run {
            // 处理规则需要内联的情况，并且尝试避免SOF
            if (config !is CwtPropertyConfig) return@run
            val inlinedConfig = inlineSingleAlias(config) ?: return@run
            val guardKey = inlinedConfig.singleAliasConfig?.let { "sa:${it.name}" } ?: return@run
            val newGuardStack = guardStack ?: mutableSetOf()
            if (!newGuardStack.add(guardKey)) return "..."
            return doGetDistinctKey(inlinedConfig, newGuardStack)
        }
        val children = config.configs
        return buildString {
            if (config is CwtPropertyConfig) append(config.key).append('=')
            when {
                children == null -> append(config.value)
                children.isEmpty() -> append("{}")
                else -> {
                    append('{')
                    append(children.map { doGetDistinctKey(it, guardStack) }.sorted().joinToString("\u0000"))
                    append('}')
                }
            }
        }
    }

    @Optimized
    fun getInBlockKeys(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.inBlockKeys) { doGetInBlockKeys(config).optimized() }
    }

    private fun doGetInBlockKeys(config: CwtMemberConfig<*>): MutableSet<@CaseInsensitive String> {
        val keys = caseInsensitiveStringSet()
        config.configs?.forEachFast {
            if (it is CwtPropertyConfig && isInBlockKey(it)) {
                keys.add(it.key)
            }
        }
        when (config) {
            is CwtPropertyConfig -> {
                val propertyConfig = config
                propertyConfig.parentConfig?.configs?.forEachFast { c ->
                    if (!c.isSamePointer(propertyConfig) && c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true)) {
                        c.configs?.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                    }
                }
            }
            is CwtValueConfig -> {
                val propertyConfig = config.propertyConfig
                propertyConfig?.parentConfig?.configs?.forEachFast { c ->
                    if (!c.isSamePointer(propertyConfig) && c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true)) {
                        c.configs?.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                    }
                }
            }
        }

        return keys
    }

    private fun isInBlockKey(config: CwtPropertyConfig): Boolean {
        val gameType = config.configGroup.gameType
        if (config.keyExpression.type != CwtDataTypes.Constant) return false
        if (config.optionData.cardinality?.isRequired() == false) return false
        if (ParadoxInlineScriptManager.isMatched(config.key, gameType)) return false // 排除是内联脚本用法的情况
        return true
    }

    // endregion

    // region Deep Copy Methods

    @Optimized
    fun createListForDeepCopy(): MutableList<CwtMemberConfig<*>> {
        return FastList()
    }

    @Optimized
    @OptIn(ExperimentalContracts::class)
    fun createListForDeepCopy(configs: List<CwtMemberConfig<*>>?): MutableList<CwtMemberConfig<*>>? {
        contract {
            returnsNotNull() implies (configs != null)
        }
        if (configs == null) return null
        return FastList()
    }

    @Optimized
    fun deepCopyConfigs(
        containerConfig: CwtMemberConfig<*>,
        parentConfig: CwtMemberConfig<*> = containerConfig
    ): List<CwtMemberConfig<*>>? {
        return withRecursionGuard {
            val key = getKeyForDeepCopy(containerConfig)
            withRecursionCheck(key) {
                doDeepCopyConfigs(containerConfig, parentConfig)
            }
        }
    }

    @Optimized
    fun deepCopyConfigsInDeclarationConfig(
        containerConfig: CwtMemberConfig<*>,
        parentConfig: CwtMemberConfig<*> = containerConfig,
        context: CwtDeclarationConfigContext
    ): List<CwtMemberConfig<*>>? {
        return withRecursionGuard {
            val key = getKeyForDeepCopy(containerConfig)
            withRecursionCheck(key) {
                doDeepCopyConfigsInDeclarationConfig(containerConfig, context, parentConfig)
            }
        }
    }

    private fun getKeyForDeepCopy(containerConfig: CwtMemberConfig<*>): Any? {
        // NOTE 2.1.1 这里可以直接使用指针作为键，应当不会存在内存泄露或其他问题
        // NOTE 2.1.1 为了优化性能，这里可以直接检查是否引用相等
        return containerConfig.pointer.takeIf { it !== emptyPointer<PsiElement>() }
    }

    private fun doDeepCopyConfigs(
        containerConfig: CwtMemberConfig<*>,
        parentConfig: CwtMemberConfig<*>
    ): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        configs.forEachFast { config ->
            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = config.delegated(childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += doDeepCopyConfigs(config, delegatedConfig).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        CwtConfigService.injectConfigs(parentConfig, result) // 注入规则
        result.forEachFast { it.parentConfig = parentConfig } // 确保绑定了父规则
        return result // 这里需要直接返回可变列表
    }

    private fun doDeepCopyConfigsInDeclarationConfig(
        containerConfig: CwtMemberConfig<*>,
        context: CwtDeclarationConfigContext,
        parentConfig: CwtMemberConfig<*>
    ): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        configs.forEachFast f@{ config ->
            val matched = isSubtypeMatchedInDeclarationConfig(config, context)
            if (matched != null) {
                // 如果匹配子类型表达式，打平其中的子规则并加入结果，否则直接跳过
                if (matched) {
                    result += deepCopyConfigsInDeclarationConfig(config, parentConfig, context).orEmpty()
                }
                return@f
            }

            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = config.delegated(childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += deepCopyConfigsInDeclarationConfig(config, delegatedConfig, context).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        CwtConfigService.injectConfigs(parentConfig, result) // 注入规则
        result.forEachFast { it.parentConfig = parentConfig } // 确保绑定了父规则
        return result // 这里需要直接返回可变列表
    }

    private fun isSubtypeMatchedInDeclarationConfig(config: CwtMemberConfig<*>, context: CwtDeclarationConfigContext): Boolean? {
        if (config !is CwtPropertyConfig) return null
        val subtypeString = config.key.removeSurroundingOrNull("subtype[", "]") ?: return null
        val subtypeExpression = ParadoxDefinitionSubtypeExpression.resolve(subtypeString)
        val subtypes = context.definitionSubtypes
        return subtypes != null && subtypeExpression.matches(subtypes)
    }

    // endregion

    // region Inline Methods

    @Optimized
    fun inline(directiveConfig: CwtDirectiveConfig): CwtPropertyConfig {
        val other = directiveConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = other,
            keyExpression = CwtDataExpression.resolveKey(directiveConfig.name),
            configs = deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        mergeOptionData(inlined.optionData, other.optionData) // merge option data
        inlined.inlineConfig = directiveConfig
        return inlined
    }

    @Optimized
    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.value ?: return null
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        return inlineSingleAlias(config, singleAliasConfig)
    }

    @Optimized
    fun inlineSingleAlias(config: CwtPropertyConfig, singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
        // inline all value and configs
        val other = singleAliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            valueExpression = other.valueExpression,
            valueType = other.valueType,
            configs = deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        mergeOptionData(inlined.optionData, config.optionData, other.optionData) // merge option data
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    @Optimized
    fun inlineAlias(config: CwtPropertyConfig, key: String): List<CwtMemberConfig<*>>? {
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.AliasMatchLeft) return null
        val aliasName = valueExpression.value ?: return null
        val aliasConfigGroup = configGroup.aliasGroups[aliasName] ?: return null
        val aliasKeys = CwtConfigManager.getAliasKeys(configGroup, aliasName, key)
        if (aliasKeys.isEmpty()) return emptyList()
        val result = createListForDeepCopy()
        aliasKeys.forEach f1@{ aliasKey ->
            val aliasConfigs = aliasConfigGroup[aliasKey]
            if (aliasConfigs.isNullOrEmpty()) return@f1
            aliasConfigs.forEachFast f2@{ aliasConfig ->
                result += inlineAlias(config, aliasConfig) ?: return@f2
            }
        }
        val parentConfig = config.parentConfig
        if (parentConfig != null) CwtConfigService.injectConfigs(parentConfig, result)
        return result
    }

    @Optimized
    fun inlineAlias(config: CwtPropertyConfig, aliasConfig: CwtAliasConfig): CwtPropertyConfig? {
        val other = aliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = aliasConfig.subNameExpression,
            valueExpression = other.valueExpression,
            valueType = other.valueType,
            configs = deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        mergeOptionData(inlined.optionData, config.optionData, other.optionData) // merge option data
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = aliasConfig
        inlined.inlineConfig = config.inlineConfig
        val finalInlined = when (inlined.valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> inlineSingleAlias(inlined) ?: return null
            else -> inlined
        }
        return finalInlined
    }

    @Optimized
    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: CwtConfigInlineMode): CwtPropertyConfig? {
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_KEY -> if (otherConfig is CwtPropertyConfig) otherConfig.keyExpression else return null
                CwtConfigInlineMode.VALUE_TO_KEY -> CwtDataExpression.resolveKey(otherConfig.value)
                else -> config.keyExpression
            },
            valueExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> if (otherConfig is CwtPropertyConfig) CwtDataExpression.resolveValue(otherConfig.key) else return null
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueExpression
                else -> config.valueExpression
            },
            valueType = when (inlineMode) {
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueType
                CwtConfigInlineMode.KEY_TO_VALUE -> CwtType.String
                else -> config.valueType
            },
            configs = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> null
                CwtConfigInlineMode.VALUE_TO_VALUE -> deepCopyConfigs(otherConfig)
                else -> deepCopyConfigs(config)
            },
        )
        inlined.postOptimize() // do post optimization
        mergeOptionData(inlined.optionData, config.optionData) // merge option data
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineWithConfigs(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        val inlined = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = configGroup,
            valueExpresssion = CwtDataExpression.resolveBlock(),
            valueType = CwtType.Block,
            configs = configs,
        )
        mergeOptionData(inlined.optionData, config?.optionData) // merge option data
        return inlined
    }

    // endregion

    // region Merge Methods

    fun mergeConfigs(configs1: List<CwtMemberConfig<*>>, configs2: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        if (configs1.isEmpty() && configs2.isEmpty()) return emptyList()
        if (configs1.isEmpty()) return configs2
        if (configs2.isEmpty()) return configs1

        if (configs1.size == 1 && configs2.size == 1) {
            val c1 = configs1.single()
            val c2 = configs2.single()
            if (c1 is CwtValueConfig && c2 is CwtValueConfig) {
                if (c1.valueType == CwtType.Block && c2.valueType == CwtType.Block) {
                    val mergedConfigs = mergeConfigs(c1.configs.orEmpty(), c2.configs.orEmpty())
                    return listOf(inlineWithConfigs(null, mergedConfigs, c1.configGroup))
                }
                val mergedConfig = mergeValueConfig(c1, c2)
                if (mergedConfig != null) return mergedConfig.singleton.list()
            } else if (c1 is CwtPropertyConfig && c2 is CwtPropertyConfig) {
                val same = getDistinctKey(c1) == getDistinctKey(c2)
                if (same) return c1.singleton.list()
            } else {
                return emptyList()
            }
        }

        if (configs1.all { it is CwtValueConfig } && configs2.all { it is CwtValueConfig }) {
            val c1 = when {
                configs1.size == 1 -> configs1.single()
                configs2.size == 1 -> configs2.single()
                else -> null
            }?.castOrNull<CwtValueConfig>()
            val cs2 = when {
                configs1.size == 1 -> configs2
                configs2.size == 1 -> configs1
                else -> null
            }?.castOrNull<List<CwtValueConfig>>()
            if (c1 != null && cs2.isNotNullOrEmpty()) {
                val mergedConfigs = cs2.mapNotNull { c2 -> mergeValueConfig(c1, c2) }
                return mergedConfigs
            }
        }

        val m1 = configs1.associateBy { getDistinctKey(it) }
        val m2 = configs2.associateBy { getDistinctKey(it) }
        val sameKeys = m1.keys intersect m2.keys
        val sameConfigs = sameKeys.mapNotNull { m1[it] ?: m2[it] }
        return sameConfigs
    }

    @Suppress("unused")
    fun mergeConfig(config1: CwtMemberConfig<*>, config2: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if (config1 === config2) return config1 // reference equality
        if (config1 isSamePointer config2) return config1 // pointer equality
        if (getDistinctKey(config1) == getDistinctKey(config2)) return config1 // distinct key equality
        return null
    }

    fun mergeValueConfig(config1: CwtValueConfig, config2: CwtValueConfig): CwtValueConfig? {
        if (config1 === config2) return config1 // reference equality
        if (config1 isSamePointer config2) return config1 // pointer equality
        if (config1.configExpression.type == CwtDataTypes.Block || config2.configExpression.type == CwtDataTypes.Block) return null // cannot merge non-same clauses
        val expressionString = CwtConfigExpressionService.merge(config1.configExpression, config2.configExpression, config1.configGroup)
        if (expressionString == null) return null
        val merged = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = config1.configGroup,
            valueExpresssion = CwtDataExpression.resolveValue(expressionString),
        )
        mergeOptionData(merged.optionData, config1.optionData, config2.optionData) // merge option data
        return merged
    }

    fun mergeAndMatchValueConfig(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
        if (configs.isEmpty()) return false
        for ((_, config) in configs.withIndex()) {
            val e1 = configExpression // expect
            val e2 = config.configExpression // actual (e.g., from parameterized key)
            val e3 = CwtConfigExpressionService.merge(e1, e2, config.configGroup) ?: continue // merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    fun mergeOptionData(optionData: CwtOptionDataHolder, vararg sources: CwtOptionDataHolder?) {
        for ((_, source) in sources.withIndex()) {
            if (source == null) continue
            source.copyTo(optionData)
        }
    }

    // endregion
}
