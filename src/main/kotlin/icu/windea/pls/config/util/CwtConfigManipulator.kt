package icu.windea.pls.config.util

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.isBlock
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.singleAliases
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.merge
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.ep.config.CwtInjectedConfigProvider
import icu.windea.pls.ep.configExpression.CwtDataExpressionMerger
import icu.windea.pls.lang.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStringConstants

object CwtConfigManipulator {
    // region Core Methods

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
            is CwtPropertyConfig -> {
                when {
                    config.configs == null -> "${config.key}=${config.value}"
                    config.configs.isNullOrEmpty() -> "${config.key}={}"
                    else -> {
                        val v = config.configs!!.joinToString("\u0000") { doGetDistinctKey(it, guardStack) }
                        return "${config.key}={${v}}"
                    }
                }
            }
            is CwtValueConfig -> {
                when {
                    config.configs == null -> config.value
                    config.configs.isNullOrEmpty() -> "{}"
                    else -> {
                        val v = config.configs!!.joinToString("\u0000") { doGetDistinctKey(it, guardStack) }
                        return "{${v}}"
                    }
                }
            }
        }
    }

    // endregion

    // region Deep Copy Methods

    fun deepCopyConfigs(config: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = config): List<CwtMemberConfig<*>>? {
        val cs1 = config.configs
        if (cs1.isNullOrEmpty()) return cs1
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEach f1@{ c1 ->
            val configs = deepCopyConfigs(c1)
            val c1Delegated = when (c1) {
                is CwtPropertyConfig -> CwtPropertyConfig.delegated(c1, configs, parentConfig)
                is CwtValueConfig -> CwtValueConfig.delegated(c1, configs, parentConfig)
            }
            result += c1Delegated
        }
        CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        return result
    }

    fun deepCopyConfigsInDeclarationConfig(config: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = config, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>>? {
        val cs1 = config.configs
        if (cs1.isNullOrEmpty()) return cs1
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEach f1@{ c1 ->
            if (c1 is CwtPropertyConfig) {
                val subtypeExpression = c1.key.removeSurroundingOrNull("subtype[", "]")
                if (subtypeExpression != null) {
                    val subtypes = context.definitionSubtypes
                    if (subtypes == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                        val cs2 = deepCopyConfigsInDeclarationConfig(c1, c1, context)
                        if (cs2.isNullOrEmpty()) return@f1
                        result += cs2
                    }
                    return@f1
                }
            }

            val configs = deepCopyConfigsInDeclarationConfig(c1, c1, context)
            val c1Delegated = when (c1) {
                is CwtPropertyConfig -> CwtPropertyConfig.delegated(c1, configs, parentConfig)
                is CwtValueConfig -> CwtValueConfig.delegated(c1, configs, parentConfig)
            }
            result += c1Delegated
        }
        CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        return result
    }

    // endregion

    // region Inline Methods

    enum class InlineMode {
        KEY_TO_KEY, KEY_TO_VALUE, VALUE_TO_KEY, VALUE_TO_VALUE
    }

    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: InlineMode): CwtPropertyConfig? {
        val inlined = CwtPropertyConfig.copy(
            targetConfig = config,
            key = when (inlineMode) {
                InlineMode.KEY_TO_KEY -> if (otherConfig is CwtPropertyConfig) otherConfig.key else return null
                InlineMode.VALUE_TO_KEY -> otherConfig.value
                else -> config.key
            },
            value = when (inlineMode) {
                InlineMode.VALUE_TO_VALUE -> otherConfig.value
                InlineMode.KEY_TO_VALUE -> if (otherConfig is CwtPropertyConfig) otherConfig.key else return null
                else -> config.value
            },
            valueType = when (inlineMode) {
                InlineMode.VALUE_TO_VALUE -> otherConfig.valueType
                InlineMode.KEY_TO_VALUE -> CwtType.String
                else -> config.valueType
            },
            configs = when (inlineMode) {
                InlineMode.KEY_TO_VALUE -> null
                InlineMode.VALUE_TO_VALUE -> deepCopyConfigs(otherConfig)
                else -> deepCopyConfigs(config)
            },
        )
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineWithConfigs(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = configGroup,
            value = PlsStringConstants.blockFolder,
            valueType = CwtType.Block,
            configs = configs,
            optionConfigs = config?.optionConfigs
        )
    }

    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.value ?: return null
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        return singleAliasConfig.inline(config)
    }

    // endregion

    // region Merge Methods

    fun mergeConfigs(configs: List<CwtMemberConfig<*>>, otherConfigs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        if (configs.isEmpty() && otherConfigs.isEmpty()) return emptyList()
        if (configs.isEmpty()) return otherConfigs
        if (otherConfigs.isEmpty()) return configs

        if (configs.size == 1 && otherConfigs.size == 1) {
            val c1 = configs.single()
            val c2 = otherConfigs.single()
            if (c1 is CwtValueConfig && c2 is CwtValueConfig) {
                if (c1.isBlock && c2.isBlock) {
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

        if (configs.all { it is CwtValueConfig } && otherConfigs.all { it is CwtValueConfig }) {
            val c1 = when {
                configs.size == 1 -> configs.single()
                otherConfigs.size == 1 -> otherConfigs.single()
                else -> null
            }?.castOrNull<CwtValueConfig>()
            val cs2 = when {
                configs.size == 1 -> otherConfigs
                otherConfigs.size == 1 -> configs
                else -> null
            }?.castOrNull<List<CwtValueConfig>>()
            if (c1 != null && cs2.isNotNullOrEmpty()) {
                val mergedConfigs = cs2.mapNotNull { c2 -> mergeValueConfig(c1, c2) }
                return mergedConfigs
            }
        }

        val m1 = configs.associateBy { getDistinctKey(it) }
        val m2 = otherConfigs.associateBy { getDistinctKey(it) }
        val sameKeys = m1.keys intersect m2.keys
        val sameConfigs = sameKeys.mapNotNull { m1[it] ?: m2[it] }
        return sameConfigs
    }

    fun mergeConfig(c1: CwtMemberConfig<*>, c2: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if (c1 === c2) return c1 //reference equality
        if (c1.pointer == c2.pointer) return c1 //value equality (should be)
        if (getDistinctKey(c1) == getDistinctKey(c2)) return c1 //distinct key equality
        return null
    }

    fun mergeValueConfig(c1: CwtValueConfig, c2: CwtValueConfig): CwtValueConfig? {
        if (c1 === c2) return c1 //reference equality
        if (c1.pointer == c2.pointer) return c1 //value equality (should be)
        if (c1.configExpression.type == CwtDataTypes.Block || c2.configExpression.type == CwtDataTypes.Block) return null //cannot merge non-same clauses
        val expressionString = CwtDataExpressionMerger.merge(c1.configExpression, c2.configExpression, c1.configGroup)
        if (expressionString == null) return null
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = c1.configGroup,
            value = expressionString,
            optionConfigs = mergeOptions(c1.optionConfigs, c2.optionConfigs)
        )
    }

    fun mergeAndMatchValueConfig(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
        if (configs.isEmpty()) return false
        for (config in configs) {
            val e1 = configExpression //expect
            val e2 = config.configExpression //actual (e.g., from parameterized key)
            val e3 = CwtDataExpressionMerger.merge(e1, e2, config.configGroup) ?: continue //merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    private fun mergeOptions(a: List<CwtOptionMemberConfig<*>>?, b: List<CwtOptionMemberConfig<*>>?): List<CwtOptionMemberConfig<*>> {
        //keep duplicate options here (no affect to features)
        return merge(a, b)
    }

    //endregion
}
