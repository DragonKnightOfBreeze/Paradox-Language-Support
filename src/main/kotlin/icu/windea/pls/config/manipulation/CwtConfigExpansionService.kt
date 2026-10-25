package icu.windea.pls.config.manipulation

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtExpandableConfig
import icu.windea.pls.config.config.CwtExpandedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.config.detached.CwtExpandedAliasConfig
import icu.windea.pls.config.config.detached.CwtExpandedSingleAliasConfig
import icu.windea.pls.config.config.detached.CwtExpandedUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.annotations.RecursionSensitive
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.stream.BiPushStream
import icu.windea.pls.core.util.stream.PushStream
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression

@Optimized
object CwtConfigExpansionService {
    // 3.0.5 optimize: public methods here should be returning push stream directly, instead of receiving processor as an argument

    /**
     * 递归展开 [config] 的子规则中的所有形如 `subtype[{subtypeExpression}] = {...}` 的属性规则中的子规则，保留其他形式的子规则。
     *
     * 返回的 [BiPushStream] 中的第一个元素是展开后的子规则，第二个元素是合并后的当前子类型表达式。
     *
     * @see ParadoxDefinitionSubtypeExpression
     */
    fun expandBySubtypeExpression(config: CwtMemberConfig<*>?): BiPushStream<CwtMemberConfig<*>, String> {
        if (config == null) return BiPushStream.of()
        return BiPushStream.from {
            expandBySubtypeExpressionWith(config, "")
        }
    }

    context(processor: (CwtMemberConfig<*>, String) -> Boolean)
    private fun expandBySubtypeExpressionWith(config: CwtMemberConfig<*>, currentExpression: String): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        config.configs?.orNull()?.forEachFast { childConfig ->
            val nextExpression = CwtConfigManipulationService.extractSubtypeExpression(childConfig)
            if (nextExpression != null) {
                if (childConfig.configs?.orNull() != null) {
                    val mergedExpression = CwtConfigManipulationService.mergeSubtypeExpression(currentExpression, nextExpression)
                    expandBySubtypeExpressionWith(childConfig, mergedExpression).let { if (!it) return false }
                }
            } else {
                processor(childConfig, currentExpression).let { if (!it) return false }
            }
        }
        return true
    }

    fun expandConfigExpression(config: CwtConfig<*>?, forValue: Boolean = false): PushStream<CwtDataExpression> {
        if (config == null) return PushStream.of()
        return PushStream.from {
            expandConfigExpressionWith(config, forValue)
        }
    }

    fun expandConfigExpression(configs: Collection<CwtConfig<*>>?, forValue: Boolean = false): PushStream<CwtDataExpression> {
        if (configs.isNullOrEmpty()) return PushStream.of()
        return PushStream.from {
            expandConfigExpressionWith(configs, forValue)
        }
    }

    context(processor: (CwtDataExpression) -> Boolean)
    private fun expandConfigExpressionWith(config: CwtConfig<*>, forValue: Boolean): Boolean {
        val configExpression = if (forValue && config is CwtMemberConfig) config.valueExpression else config.configExpression
        val configGroup = config.configGroup
        return expandConfigExpressionWith(configExpression, configGroup)
    }

    context(processor: (CwtDataExpression) -> Boolean)
    private fun expandConfigExpressionWith(configs: Collection<CwtConfig<*>>, forValue: Boolean): Boolean {
        return configs.process { config ->
            val configExpression = if (forValue && config is CwtMemberConfig) config.valueExpression else config.configExpression
            val configGroup = config.configGroup
            expandConfigExpressionWith(configExpression, configGroup)
        }
    }

    context(processor: (CwtDataExpression) -> Boolean)
    private fun expandConfigExpressionWith(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup): Boolean {
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
     * @see CwtExpandedUnionConfig
     */
    @RecursionSensitive
    fun expandUnion(name: String?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedUnionConfig> {
        if (name.isNullOrEmpty()) return PushStream.of()
        return PushStream.from {
            runWithRecursionGuard(guardName, "u:$name") {
                val model = configGroup.unionModel
                val consts = model.expandedForConst[name]
                if (consts != null) {
                    for (config in consts.values) if (!invoke(config)) return@runWithRecursionGuard false
                }
                val nonConsts = model.expandedForNonConst[name]
                if (nonConsts != null) {
                    for (config in nonConsts) if (!invoke(config)) return@runWithRecursionGuard false
                }
                true
            }
        }
    }

    /**
     * 将 [name] 作为别名规则的名字，展开此规则的所有候选项。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtAliasConfig
     * @see CwtExpandedAliasConfig
     */
    @RecursionSensitive
    fun expandAlias(name: String?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedAliasConfig> {
        if (name.isNullOrEmpty()) return PushStream.of()
        return PushStream.from {
            runWithRecursionGuard(guardName, "a:$name") {
                val model = configGroup.aliasModel
                val consts = model.expandedForConst[name]
                if (consts != null) {
                    for (config in consts.values) if (!invoke(config)) return@runWithRecursionGuard false
                }
                val nonConsts = model.expandedForNonConst[name]
                if (nonConsts != null) {
                    for (config in nonConsts) if (!invoke(config)) return@runWithRecursionGuard false
                }
                true
            }
        }
    }

    /**
     * 将 [name] 作为单别名规则的名字，展开此规则的所有候选项。
     *
     * @see CwtSingleAliasConfig
     * @see CwtExpandedSingleAliasConfig
     */
    fun expandSingleAlias(name: String?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedSingleAliasConfig> {
        if (name.isNullOrEmpty()) return PushStream.of()
        return PushStream.from {
            runWithRecursionGuard(guardName, "sa:$name") {
                val config = configGroup.singleAliasModel.expanded[name] ?: return@runWithRecursionGuard true
                invoke(config)
            }
        }
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
    fun expandUnion(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedUnionConfig> {
        if (configExpression == null) return PushStream.of()
        if (configExpression.type != CwtDataTypes.UnionValue) return PushStream.of()
        val name = configExpression.metadata.value
        return expandUnion(name, configGroup, guardName)
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
    fun expandAlias(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedAliasConfig> {
        if (configExpression == null) return PushStream.of()
        if (configExpression.type != CwtDataTypes.AliasName && configExpression.type != CwtDataTypes.AliasKeysField) return PushStream.of()
        val name = configExpression.metadata.value
        return expandAlias(name, configGroup, guardName)
    }

    /**
     * 从 [configExpression] 中提取单别名规则的名字，然后展开此规则的所有候选项。
     *
     * @see CwtSingleAliasConfig
     * @see CwtDataTypes.SingleAliasRight
     */
    fun expandSingleAlias(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedSingleAliasConfig> {
        if (configExpression == null) return PushStream.of()
        if (configExpression.type != CwtDataTypes.SingleAliasRight) return PushStream.of()
        val name = configExpression.metadata.value
        return expandSingleAlias(name, configGroup, guardName)
    }

    /**
     * 从 [configExpression] 中提取可展开的规则的名字，然后展开此规则的所有候选项对应的规则。
     *
     * **注意**：调用时需要避免递归。
     *
     * @see CwtExpandableConfig
     * @see CwtDataTypeSets.Expandable
     */
    @RecursionSensitive
    fun expandExpandable(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, guardName: String): PushStream<CwtExpandedConfig<PsiElement>> {
        if (configExpression == null) return PushStream.of()
        if (configExpression.type !in CwtDataTypeSets.Expandable) return PushStream.of()
        val name = configExpression.metadata.value ?: return PushStream.of()
        return when (configExpression.type) {
            CwtDataTypes.UnionValue -> expandUnion(name, configGroup, guardName)
            CwtDataTypes.AliasName, CwtDataTypes.AliasKeysField -> expandAlias(name, configGroup, guardName)
            CwtDataTypes.SingleAliasRight -> expandSingleAlias(name, configGroup, guardName)
            else -> PushStream.of()
        }
    }
}
