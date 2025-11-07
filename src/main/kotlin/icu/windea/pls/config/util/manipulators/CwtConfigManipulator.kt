package icu.windea.pls.config.util.manipulators

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.singleAliases
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.annotations.Fast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.merge
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.ep.config.CwtInjectedConfigProvider
import icu.windea.pls.ep.configExpression.CwtDataExpressionMerger
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStringConstants
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object CwtConfigManipulator {
    // region Common Methods

    fun getShallowKey(config: CwtMemberConfig<*>): String {
        return doGetShallowKey(config)
    }

    private fun doGetShallowKey(config: CwtMemberConfig<*>): String {
        return when (config) {
            is CwtPropertyConfig -> when {
                config.configs == null -> "${config.key}=${config.value}"
                else -> "${config.key}={}"
            }
            is CwtValueConfig -> when {
                config.configs == null -> config.value
                else -> "{}"
            }
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
        return when (config) {
            is CwtPropertyConfig -> when {
                config.configs == null -> "${config.key}=${config.value}"
                config.configs.isNullOrEmpty() -> "${config.key}={}"
                else -> {
                    val v = config.configs!!.joinToString("\u0000") { doGetDistinctKey(it, guardStack) }
                    return "${config.key}={${v}}"
                }
            }
            is CwtValueConfig -> when {
                config.configs == null -> config.value
                config.configs.isNullOrEmpty() -> "{}"
                else -> {
                    val v = config.configs!!.joinToString("\u0000") { doGetDistinctKey(it, guardStack) }
                    return "{${v}}"
                }
            }
        }
    }

    // endregion

    // region Deep Copy Methods

    @Fast
    fun createListForDeepCopy(): MutableList<CwtMemberConfig<*>> {
        return ObjectArrayList()
    }

    @Fast
    @OptIn(ExperimentalContracts::class)
    fun createListForDeepCopy(configs: List<CwtMemberConfig<*>>?): MutableList<CwtMemberConfig<*>>? {
        contract {
            returnsNotNull() implies (configs != null)
        }
        if (configs == null) return null
        return ObjectArrayList()
    }

    fun deepCopyConfigs(
        containerConfig: CwtMemberConfig<*>,
        parentConfig: CwtMemberConfig<*> = containerConfig
    ): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        for (config in configs) {
            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = CwtMemberConfig.delegated(config, childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += deepCopyConfigs(config, delegatedConfig).orEmpty()
            CwtMemberConfig.postOptimize(delegatedConfig) // 进行后续优化
            result += delegatedConfig
        }
        CwtInjectedConfigProvider.injectConfigs(parentConfig, result) // 注入规则
        result.forEach { it.parentConfig = parentConfig } // 确保绑定了父规则
        return result // 这里需要直接返回可变列表
    }

    fun deepCopyConfigsInDeclarationConfig(
        containerConfig: CwtMemberConfig<*>,
        parentConfig: CwtMemberConfig<*> = containerConfig,
        context: CwtDeclarationConfigContext
    ): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        for (config in configs) {
            val matched = isSubtypeMatchedInDeclarationConfig(config, context)
            if (matched != null) {
                // 如果匹配子类型表达式，打平其中的子规则并加入结果，否则直接跳过
                if (matched) {
                    result += deepCopyConfigsInDeclarationConfig(config, parentConfig, context).orEmpty()
                }
                continue
            }

            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = CwtMemberConfig.delegated(config, childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += deepCopyConfigsInDeclarationConfig(config, delegatedConfig, context).orEmpty()
            CwtMemberConfig.postOptimize(delegatedConfig) // 进行后续优化
            result += delegatedConfig
        }
        CwtInjectedConfigProvider.injectConfigs(parentConfig, result) // 注入规则
        result.forEach { it.parentConfig = parentConfig } // 确保绑定了父规则
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

    fun inline(config: CwtInlineConfig): CwtPropertyConfig {
        val other = config.config
        val inlined = CwtPropertyConfig.copy(
            targetConfig = other,
            key = config.name,
            configs = deepCopyConfigs(other)
        )
        CwtPropertyConfig.postOptimize(inlined) // 进行后续优化
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = config
        return inlined
    }

    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.value ?: return null
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        return inlineSingleAlias(config, singleAliasConfig)
    }

    fun inlineSingleAlias(config: CwtPropertyConfig, singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
        // inline all value and configs
        val other = singleAliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            targetConfig = config,
            value = other.value,
            valueType = other.valueType,
            configs = deepCopyConfigs(other),
            optionConfigs = config.optionConfigs,
        )
        CwtPropertyConfig.postOptimize(inlined) // 进行后续优化
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.singleAliasConfig = singleAliasConfig
        return inlined
    }

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
            aliasConfigs.forEach f2@{ aliasConfig ->
                result += inlineAlias(config, aliasConfig) ?: return@f2
            }
        }
        val parentConfig = config.parentConfig
        if (parentConfig != null) CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        return result.optimized()
    }

    fun inlineAlias(config: CwtPropertyConfig, aliasConfig: CwtAliasConfig): CwtPropertyConfig? {
        val other = aliasConfig.config
        val inlined = CwtPropertyConfig.copy(
            targetConfig = config,
            key = aliasConfig.subName,
            value = other.value,
            valueType = other.valueType,
            configs = deepCopyConfigs(other),
            optionConfigs = other.optionConfigs
        )
        CwtPropertyConfig.postOptimize(inlined) // 进行后续优化
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = aliasConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        val finalInlined = when (inlined.valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> inlineSingleAlias(inlined) ?: return null
            else -> inlined
        }
        return finalInlined
    }

    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: CwtConfigInlineMode): CwtPropertyConfig? {
        val inlined = CwtPropertyConfig.copy(
            targetConfig = config,
            key = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_KEY -> if (otherConfig is CwtPropertyConfig) otherConfig.key else return null
                CwtConfigInlineMode.VALUE_TO_KEY -> otherConfig.value
                else -> config.key
            },
            value = when (inlineMode) {
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.value
                CwtConfigInlineMode.KEY_TO_VALUE -> if (otherConfig is CwtPropertyConfig) otherConfig.key else return null
                else -> config.value
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
        CwtPropertyConfig.postOptimize(inlined) // 进行后续优化
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineWithConfigs(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        return CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = configGroup,
            value = PlsStringConstants.blockFolder,
            valueType = CwtType.Block,
            configs = configs,
            optionConfigs = config?.optionConfigs.orEmpty()
        )
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
        if (config1.pointer == config2.pointer) return config1 // value equality (should be)
        if (getDistinctKey(config1) == getDistinctKey(config2)) return config1 // distinct key equality
        return null
    }

    fun mergeValueConfig(config1: CwtValueConfig, config2: CwtValueConfig): CwtValueConfig? {
        if (config1 === config2) return config1 // reference equality
        if (config1.pointer == config2.pointer) return config1 // value equality (should be)
        if (config1.configExpression.type == CwtDataTypes.Block || config2.configExpression.type == CwtDataTypes.Block) return null // cannot merge non-same clauses
        val expressionString = CwtDataExpressionMerger.merge(config1.configExpression, config2.configExpression, config1.configGroup)
        if (expressionString == null) return null
        return CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = config1.configGroup,
            value = expressionString,
            optionConfigs = mergeOptions(config1.optionConfigs, config2.optionConfigs)
        )
    }

    fun mergeAndMatchValueConfig(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
        if (configs.isEmpty()) return false
        for (config in configs) {
            val e1 = configExpression // expect
            val e2 = config.configExpression // actual (e.g., from parameterized key)
            val e3 = CwtDataExpressionMerger.merge(e1, e2, config.configGroup) ?: continue // merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    private fun mergeOptions(options1: List<CwtOptionMemberConfig<*>>?, options2: List<CwtOptionMemberConfig<*>>?): List<CwtOptionMemberConfig<*>> {
        // keep duplicate options here (no affect to features)
        return merge(options1, options2)
    }

    // endregion
}
