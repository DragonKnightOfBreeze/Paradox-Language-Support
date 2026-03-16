package icu.windea.pls.config.util.manipulators

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtConfigInlineMode
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
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
import icu.windea.pls.config.util.CwtMemberConfigVisitor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.CwtType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object CwtConfigManipulator {
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
    fun deepCopyConfigs(containerConfig: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = containerConfig): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigs(containerConfig, parentConfig)
    }

    @Optimized
    fun deepCopyConfigsInDeclaration(containerConfig: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = containerConfig, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigsInDeclaration(containerConfig, context, parentConfig)
    }

    private fun doDeepCopyConfigs(containerConfig: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
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
        result.forEachFast { it.parentConfig = parentConfig } // 确保绑定了父规则
        injectConfigsForDeepCopy(parentConfig, result) ?: return emptyList() // 尝试注入规则，如果失败则返回空列表（即使输入的结果为空也要尝试）
        return result // 这里需要直接返回可变列表
    }

    private fun doDeepCopyConfigsInDeclaration(containerConfig: CwtMemberConfig<*>, context: CwtDeclarationConfigContext, parentConfig: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
        val configs = containerConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy()
        configs.forEachFast f@{ config ->
            val matched = isSubtypeMatchedInDeclarationConfig(config, context)
            if (matched != null) {
                // 如果匹配子类型表达式，打平其中的子规则并加入结果，否则直接跳过
                if (matched) {
                    result += deepCopyConfigsInDeclaration(config, parentConfig, context).orEmpty()
                }
                return@f
            }

            val childConfigs = createListForDeepCopy(config.configs)
            val delegatedConfig = config.delegated(childConfigs).also { it.parentConfig = parentConfig }
            if (childConfigs != null) childConfigs += deepCopyConfigsInDeclaration(config, delegatedConfig, context).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        result.forEachFast { it.parentConfig = parentConfig } // 确保绑定了父规则
        injectConfigsForDeepCopy(parentConfig, result) ?: return emptyList() // 尝试注入规则，如果失败则返回空列表（即使输入的结果为空也要尝试）
        return result // 这里需要直接返回可变列表
    }

    private fun isSubtypeMatchedInDeclarationConfig(config: CwtMemberConfig<*>, context: CwtDeclarationConfigContext): Boolean? {
        if (config !is CwtPropertyConfig) return null
        val subtypeString = config.key.removeSurroundingOrNull("subtype[", "]") ?: return null
        val subtypeExpression = ParadoxDefinitionSubtypeExpression.resolve(subtypeString)
        val subtypes = context.definitionSubtypes
        return subtypes != null && subtypeExpression.matches(subtypes)
    }

    private fun injectConfigsForDeepCopy(parentConfig: CwtMemberConfig<*>, result: MutableList<CwtMemberConfig<*>>): Boolean? {
        // NOTE 2.1.1 对于目前的深拷贝规则的逻辑，仅需在注入规则时使用递归守卫（根据分析结果，无需使用命名递归守卫）
        return withRecursionGuard {
            val key = getKeyForDeepCopy(parentConfig)
            withRecursionCheck(key) {
                CwtConfigService.injectConfigs(parentConfig, result)
            }
        }
    }

    private fun getKeyForDeepCopy(parentConfig: CwtMemberConfig<*>): Any? {
        // NOTE 2.1.1 这里可以直接使用指针作为键，应当不会存在内存泄露或其他问题
        // NOTE 2.1.1 为了优化性能，这里可以直接检查是否引用相等
        return parentConfig.pointer.takeIf { it !== emptyPointer<PsiElement>() }
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
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.value ?: return null
        val configGroup = config.configGroup
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
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.AliasMatchLeft) return null
        val aliasName = valueExpression.value ?: return null
        val configGroup = config.configGroup
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

    @Optimized
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

    // region Visit Methods

    fun visitInlined(config: CwtPropertyConfig, forSingleAlias: Boolean = true, forAlias: Boolean = true, visitor: CwtMemberConfigVisitor): Boolean {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> {
                if (!forSingleAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForSingleAlias(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasMatchLeft -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                val keyExpression = config.keyExpression
                if (keyExpression.type != CwtDataTypes.AliasName || keyExpression.value != name) return true // invalid
                visitInlinedForAlias(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasKeysField -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForAlias(name, config.configGroup, visitor)
            }
            else -> true
        }
    }

    fun visitInlined(config: CwtValueConfig, forSingleAlias: Boolean = true, forAlias: Boolean = true, visitor: CwtMemberConfigVisitor): Boolean {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> {
                if (!forSingleAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForSingleAlias(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasMatchLeft -> {
                true // ignored (must be processed on property config level)
            }
            CwtDataTypes.AliasKeysField -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForAlias(name, config.configGroup, visitor)
            }
            else -> true
        }
    }

    private fun visitInlinedForSingleAlias(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 2.1.6 recursion guard is required here
        val singleAliasConfig = configGroup.singleAliases[name] ?: return true
        return withRecursionGuard {
            withRecursionCheck("sa:$name") {
                singleAliasConfig.config.accept(visitor)
            }
        } ?: true
    }

    private fun visitInlinedForAlias(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 2.1.6 recursion guard is required here
        val aliasConfigGroup = configGroup.aliasGroups[name]?.values?.orNull() ?: return true
        return withRecursionGuard {
            withRecursionCheck("a:$name") check@{
                aliasConfigGroup.forEach { aliasConfigs ->
                    aliasConfigs.forEachFast { aliasConfig ->
                        val r = aliasConfig.config.accept(visitor)
                        if (!r) return@check false
                    }
                }
                true
            }
        } ?: true
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
                if (mergedConfig != null) return mergedConfig.to.singletonList()
            } else if (c1 is CwtPropertyConfig && c2 is CwtPropertyConfig) {
                val same = CwtConfigKeyManipulator.getDistinctKey(c1) == CwtConfigKeyManipulator.getDistinctKey(c2)
                if (same) return c1.to.singletonList()
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

        val m1 = configs1.associateBy { CwtConfigKeyManipulator.getDistinctKey(it) }
        val m2 = configs2.associateBy { CwtConfigKeyManipulator.getDistinctKey(it) }
        val sameKeys = m1.keys intersect m2.keys
        val sameConfigs = sameKeys.mapNotNull { m1[it] ?: m2[it] }
        return sameConfigs
    }

    @Suppress("unused")
    fun mergeConfig(config1: CwtMemberConfig<*>, config2: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if (config1 === config2) return config1 // reference equality
        if (config1 isSamePointer config2) return config1 // pointer equality
        if (CwtConfigKeyManipulator.getDistinctKey(config1) == CwtConfigKeyManipulator.getDistinctKey(config2)) return config1 // distinct key equality
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
        for (config in configs) {
            val e1 = configExpression // expect
            val e2 = config.configExpression // actual (e.g., from parameterized key)
            val e3 = CwtConfigExpressionService.merge(e1, e2, config.configGroup) ?: continue // merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    fun mergeOptionData(optionData: CwtOptionDataHolder, vararg sources: CwtOptionDataHolder?) {
        for (source in sources) {
            if (source == null) continue
            source.copyTo(optionData)
        }
    }

    // endregion
}
