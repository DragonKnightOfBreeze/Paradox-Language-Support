package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtExpandableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.RecursionSensitive
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression

object CwtConfigExpansionService {
    /**
     * 递归展开 [config] 的子规则中的所有形如 `subtype[{subtypeExpression}] = {...}` 的属性规则中的子规则，保留其他形式的子规则。
     *
     * 结果序列中的元组的第一个元素是展开后的子规则，第二个元素是合并后的当前子类型表达式。
     *
     * @see ParadoxDefinitionSubtypeExpression
     */
    fun expandBySubtypeExpression(config: CwtMemberConfig<*>?, processor: (r: CwtMemberConfig<*>, e: String) -> Boolean): Boolean {
        if (config == null) return true
        return doExpandBySubtypeExpression(config, "", processor)
    }

    private fun doExpandBySubtypeExpression(config: CwtMemberConfig<*>, currentExpression: String, processor: (r: CwtMemberConfig<*>, e: String) -> Boolean): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        config.configs?.orNull()?.forEachFast { childConfig ->
            val nextExpression = CwtConfigManipulationService.extractSubtypeExpression(childConfig)
            if (nextExpression != null) {
                if (childConfig.configs?.orNull() != null) {
                    val mergedExpression = CwtConfigManipulationService.mergeSubtypeExpression(currentExpression, nextExpression)
                    doExpandBySubtypeExpression(childConfig, mergedExpression, processor).let { if (!it) return false }
                }
            } else {
                processor(childConfig, currentExpression).let { if (!it) return false }
            }
        }
        return true
    }

    fun expandConfigExpression(config: CwtConfig<*>?, forValue: Boolean = false, processor: (e: CwtDataExpression) -> Boolean): Boolean {
        if (config == null) return true
        val configExpression = if (forValue && config is CwtMemberConfig) config.valueExpression else config.configExpression
        val configGroup = config.configGroup
        return doExpandConfigExpression(configExpression, configGroup, processor)
    }

    fun expandConfigExpression(configs: Collection<CwtConfig<*>>?, forValue: Boolean = false, processor: (e: CwtDataExpression) -> Boolean): Boolean {
        if (configs.isNullOrEmpty()) return true
        return configs.process { config ->
            val configExpression = if (forValue && config is CwtMemberConfig) config.valueExpression else config.configExpression
            val configGroup = config.configGroup
            doExpandConfigExpression(configExpression, configGroup, processor)
        }
    }

    private fun doExpandConfigExpression(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, processor: (e: CwtDataExpression) -> Boolean): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        if (configExpression == null) return true
        return when (configExpression.type) {
            CwtDataTypes.UnionValue -> {
                val name = configExpression.metadata.value ?: return true
                configGroup.unions[name]?.valueConfigs?.orNull()?.processFast { e -> processor(e.valueExpression) } ?: true
            }
            CwtDataTypes.AliasKeysField -> {
                val name = configExpression.metadata.value ?: return true
                configGroup.aliasGroups[name]?.values?.orNull()?.process { e -> processor(e.first().subNameExpression) } ?: true
            }
            else -> processor(configExpression)
        }
    }

    /**
     * 将 [name] 作为并集规则的名字，展开此规则的所有候选项。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtUnionConfig
     */
    @RecursionSensitive
    fun expandUnion(name: String?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression, r: CwtValueConfig) -> Boolean): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        if (name.isNullOrEmpty()) return true
        val unionConfig = configGroup.unions[name] ?: return true
        val valueConfigs = unionConfig.valueConfigs
        if (valueConfigs.isEmpty()) return true
        val recursionGuardKey = "u:$name"
        return runWithRecursionGuard(recursionGuardName, recursionGuardKey) {
            valueConfigs.processFast { valueConfig ->
                processor(valueConfig.configExpression, valueConfig)
            }
        } ?: true
    }

    /**
     * 将 [name] 作为别名规则的名字，展开此规则的所有候选项。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtAliasConfig
     */
    @RecursionSensitive
    fun expandAlias(name: String?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression, r: List<CwtAliasConfig>) -> Boolean): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        if (name.isNullOrEmpty()) return true
        val aliasGroup = configGroup.aliasGroups[name] ?: return true
        if (aliasGroup.isEmpty()) return true
        val recursionGuardKey = "a:$name"
        return runWithRecursionGuard(recursionGuardName, recursionGuardKey) {
            aliasGroup.values.process p@{ aliasConfigs ->
                val aliasExpression = aliasConfigs.firstOrNull()?.configExpression ?: return@p true
                processor(aliasExpression, aliasConfigs)
            }
        } ?: true
    }

    /**
     * 将 [name] 作为单别名规则的名字，展开此规则的所有候选项。
     *
     * @see CwtSingleAliasConfig
     */
    fun expandSingleAlias(name: String?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression, r: CwtSingleAliasConfig) -> Boolean): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        if (name.isNullOrEmpty()) return true
        val singleAliasConfig = configGroup.singleAliases[name] ?: return true
        val recursionGuardKey = "sa:$name"
        return runWithRecursionGuard(recursionGuardName, recursionGuardKey) {
            processor(singleAliasConfig.config.valueExpression, singleAliasConfig)
        } ?: true
    }

    /**
     * 从 [configExpression] 中提取并集规则的名字，然后展开此规则的所有候选项。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtUnionConfig
     * @see CwtDataTypes.UnionValue
     */
    @RecursionSensitive
    fun expandUnion(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression, r: CwtValueConfig) -> Boolean): Boolean {
        if (configExpression == null) return true
        if (configExpression.type != CwtDataTypes.UnionValue) return true
        val name = configExpression.metadata.value ?: return true
        return expandUnion(name, configGroup, recursionGuardName, processor)
    }

    /**
     * 从 [configExpression] 中提取别名规则的名字，然后展开此规则的所有候选项。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtAliasConfig
     * @see CwtDataTypes.AliasName
     * @see CwtDataTypes.AliasKeysField
     */
    @RecursionSensitive
    fun expandAlias(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression, r: List<CwtAliasConfig>) -> Boolean): Boolean {
        if (configExpression == null) return true
        if (configExpression.type != CwtDataTypes.AliasName && configExpression.type != CwtDataTypes.AliasKeysField) return true
        val name = configExpression.metadata.value ?: return true
        return expandAlias(name, configGroup, recursionGuardName, processor)
    }

    /**
     * 从 [configExpression] 中提取单别名规则的名字，然后展开此规则的所有候选项。
     *
     * @see CwtSingleAliasConfig
     * @see CwtDataTypes.SingleAliasRight
     */
    fun expandSingleAlias(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression, r: CwtSingleAliasConfig) -> Boolean): Boolean {
        if (configExpression == null) return true
        if (configExpression.type != CwtDataTypes.SingleAliasRight) return true
        val name = configExpression.metadata.value ?: return true
        return expandSingleAlias(name, configGroup, recursionGuardName, processor)
    }

    /**
     * 从 [configExpression] 中提取可展开的规则的名字，然后展开此规则的所有候选项对应的数据表达式。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtExpandableConfig
     * @see CwtDataTypeSets.Expandable
     */
    @RecursionSensitive
    fun expandExpandable(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, recursionGuardName: String, processor: (e: CwtDataExpression) -> Boolean): Boolean {
        if (configExpression == null) return true
        if (configExpression.type !in CwtDataTypeSets.Expandable) return true
        val name = configExpression.metadata.value ?: return true
        return when (configExpression.type) {
            CwtDataTypes.UnionValue -> expandUnion(name, configGroup, recursionGuardName) { e, _ -> processor(e) }
            CwtDataTypes.AliasName, CwtDataTypes.AliasKeysField -> expandAlias(name, configGroup, recursionGuardName) { e, _ -> processor(e) }
            CwtDataTypes.SingleAliasRight -> expandSingleAlias(name, configGroup, recursionGuardName) { e, _ -> processor(e) }
            else -> true
        }
    }
}
