package icu.windea.pls.config.manipulation

import com.intellij.psi.PsiElement
import com.intellij.util.SmartList
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.isSamePointer
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.option.CwtOptionMetadata
import icu.windea.pls.config.util.CwtConfigKeyManager
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.allFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.collections.processFast
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.type.CwtExpressionType

@Optimized
object CwtConfigManipulationService {
    // region Common Methods

    fun createListForDeepCopy(): MutableList<CwtMemberConfig<*>> {
        return SmartList() // 3.0.1 optimize: use SmartList here (reduce temporary memory overhead, especially for the sizes of 0 and 1)
    }

    fun createListForDeepCopy(expectedSize: Int): MutableList<CwtMemberConfig<*>> {
        require(expectedSize >= 0) { "expectedSize must be non-negative" }
        if (expectedSize <= 1) return SmartList() // 3.0.1 optimize: use SmartList here (reduce temporary memory overhead, especially for the sizes of 0 and 1)
        return ArrayList(expectedSize) // 3.0.1 optimize: use sized mutable list here
    }

    /**
     * 递归拷贝 [parentConfig] 中的所有子节点，并加入作为 [containerConfig] 的子规则。
     */
    fun deepCopyConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*> = parentConfig): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigs(parentConfig, containerConfig)
    }

    /**
     * 在声明规则上下文 [context] 中中，递归拷贝 [parentConfig] 中的所有子节点，并加入作为 [containerConfig] 的子规则。
     */
    fun deepCopyConfigsInDeclaration(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*> = parentConfig, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigsInDeclaration(parentConfig, containerConfig, context)
    }

    private fun doDeepCopyConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
        val configs = parentConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy(expectedSize = configs.size)
        configs.forEachFast { config ->
            val childConfigs = config.configs
            val childResult = if (childConfigs != null) createListForDeepCopy(expectedSize = childConfigs.size) else null
            val delegatedConfig = config.delegated(childResult).also { it.withParentConfig(containerConfig) }
            if (childResult != null) childResult += doDeepCopyConfigs(config, delegatedConfig).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        result.forEachFast { it.withParentConfig(containerConfig) } // 确保绑定了父规则
        injectConfigsForDeepCopy(parentConfig, containerConfig, result) ?: return emptyList() // 尝试注入规则，如果失败则返回空列表（即使输入的结果为空也要尝试）
        return result // 这里需要直接返回可变列表
    }

    private fun doDeepCopyConfigsInDeclaration(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>>? {
        val configs = parentConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy(/* expectedSize = configs.size */)
        configs.forEachFast f@{ config ->
            run r@{
                // 如果匹配子类型表达式，打平其中的子规则并加入结果，否则直接跳过
                val subtypes = context.definitionSubtypes ?: return@r
                val subtypeExpression = extractSubtypeExpression(config) ?: return@r
                if (config.configs.isNullOrEmpty()) return@f // skip
                val matched = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)
                if (!matched) return@f // skip
                result += deepCopyConfigsInDeclaration(config, containerConfig, context).orEmpty()
                return@f
            }

            val childConfigs = config.configs
            val childResult = if (childConfigs != null) createListForDeepCopy(/* expectedSize = childConfigs.size */) else null
            val delegatedConfig = config.delegated(childResult).also { it.withParentConfig(containerConfig) }
            if (childResult != null) childResult += deepCopyConfigsInDeclaration(config, delegatedConfig, context).orEmpty()
            delegatedConfig.postOptimize() // 进行后续优化
            result += delegatedConfig
        }
        result.forEachFast { it.withParentConfig(containerConfig) } // 确保绑定了父规则
        injectConfigsForDeepCopy(parentConfig, containerConfig, result) ?: return emptyList() // 尝试注入规则，如果失败则返回空列表（即使输入的结果为空也要尝试）
        return result // 这里需要直接返回可变列表
    }

    private fun injectConfigsForDeepCopy(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, result: MutableList<CwtMemberConfig<*>>): Boolean? {
        // NOTE 2.1.1 对于目前的深拷贝规则的逻辑，仅需在注入规则时使用递归守卫
        return withRecursionGuard("CwtConfigManipulationService.injectConfigsForDeepCopy") {
            val key = getKeyForDeepCopy(parentConfig)
            withRecursionCheck(key) {
                CwtConfigService.injectConfigs(parentConfig, containerConfig, result)
            }
        }
    }

    private fun getKeyForDeepCopy(parentConfig: CwtMemberConfig<*>): Any? {
        // NOTE 2.1.1 这里可以直接使用指针作为键，应当不会存在内存泄露或其他问题
        // NOTE 2.1.1 为了优化性能，这里可以直接检查是否引用相等
        return parentConfig.pointer.takeIf { it !== emptyPointer<PsiElement>() }
    }

    fun extractSubtypeExpression(config: CwtMemberConfig<*>): String? {
        if (config !is CwtPropertyConfig) return null
        return config.key.removeSurroundingOrNull("subtype[", "]")
    }

    fun mergeSubtypeExpression(expression: String, otherExpression: String): String {
        return when {
            expression.isEmpty() -> otherExpression
            otherExpression.isEmpty() -> expression
            expression == otherExpression -> expression
            else -> "$expression&$otherExpression"
        }
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
                if (c1.valueType == CwtExpressionType.Block && c2.valueType == CwtExpressionType.Block) {
                    val mergedConfigs = mergeConfigs(c1.configs.orEmpty(), c2.configs.orEmpty())
                    return listOf(inlineForContextConfig(null, mergedConfigs, c1.configGroup))
                }
                val mergedConfig = mergeValueConfig(c1, c2)
                if (mergedConfig != null) return mergedConfig.to.singletonList()
            } else if (c1 is CwtPropertyConfig && c2 is CwtPropertyConfig) {
                val same = CwtConfigKeyManager.getDistinctKey(c1) == CwtConfigKeyManager.getDistinctKey(c2)
                if (same) return c1.to.singletonList()
            } else {
                return emptyList()
            }
        }

        if (configs.allFast { it is CwtValueConfig } && otherConfigs.allFast { it is CwtValueConfig }) {
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
                val mergedConfigs = cs2.mapNotNullFast { c2 -> mergeValueConfig(c1, c2) }
                return mergedConfigs
            }
        }

        val m1 = configs.associateBy { CwtConfigKeyManager.getDistinctKey(it) }
        val m2 = otherConfigs.associateBy { CwtConfigKeyManager.getDistinctKey(it) }
        val sameKeys = m1.keys intersect m2.keys
        val sameConfigs = sameKeys.mapNotNull { m1[it] ?: m2[it] }
        return sameConfigs
    }

    @Suppress("unused")
    fun mergeConfig(config: CwtMemberConfig<*>, otherConfig: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if (config === otherConfig) return config // reference equality
        if (config isSamePointer otherConfig) return config // pointer equality
        if (CwtConfigKeyManager.getDistinctKey(config) == CwtConfigKeyManager.getDistinctKey(otherConfig)) return config // distinct key equality
        return null
    }

    fun mergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        if (config === otherConfig) return config // reference equality
        if (config isSamePointer otherConfig) return config // pointer equality
        if (config.configExpression.type == CwtDataTypes.Block || otherConfig.configExpression.type == CwtDataTypes.Block) return null // cannot merge non-same clauses
        val expressionString = CwtConfigExpressionManipulationService.mergeDataExpression(config.configExpression, otherConfig.configExpression)
        if (expressionString == null) return null
        val merged = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = config.configGroup,
            valueExpression = CwtDataExpression.resolve(expressionString, CwtDataExpressionRole.Value),
        )
        mergeOptionMetadata(merged.optionMetadata, config.optionMetadata, otherConfig.optionMetadata) // merge option metadata
        return merged
    }

    fun mergeOptionMetadata(optionMetadata: CwtOptionMetadata, vararg sources: CwtOptionMetadata?) {
        for (source in sources) {
            if (source == null) continue
            source.mergeTo(optionMetadata)
        }
    }

    fun mergeAndMatchValueConfigs(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
        configs.forEachFast f@{ config ->
            val e1 = configExpression // expect
            val e2 = config.configExpression // actual (e.g., from parameterized key)
            val e3 = CwtConfigExpressionManipulationService.mergeDataExpression(e1, e2) ?: return@f // merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    // endregion

    // region Inline Methods

    fun inlineAlias(config: CwtPropertyConfig, key: String): List<CwtMemberConfig<*>>? {
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.AliasMatchLeft) return null
        val aliasName = valueExpression.metadata.value ?: return null
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
        if (parentConfig != null) CwtConfigService.injectConfigs(parentConfig, parentConfig, result)
        return result
    }

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
        mergeOptionMetadata(inlined.optionMetadata, config.optionMetadata, other.optionMetadata) // merge option metadata
        inlined.withParentConfig(config.parentConfig)
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = aliasConfig
        inlined.inlineConfig = config.inlineConfig
        val finalInlined = when (inlined.valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> inlineSingleAlias(inlined) ?: return null
            else -> inlined
        }
        return finalInlined
    }

    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        val valueExpression = config.valueExpression
        if (valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.metadata.value ?: return null
        val configGroup = config.configGroup
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        return inlineSingleAlias(config, singleAliasConfig)
    }

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
        mergeOptionMetadata(inlined.optionMetadata, config.optionMetadata, other.optionMetadata) // merge option metadata
        inlined.withParentConfig(config.parentConfig)
        inlined.singleAliasConfig = singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineMacro(macroConfig: CwtMacroConfig.InlineScript): CwtPropertyConfig {
        val other = macroConfig.contextContainerConfig
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = other,
            keyExpression = CwtDataExpression.resolve(macroConfig.name, CwtDataExpressionRole.Key),
            configs = deepCopyConfigs(other),
        )
        inlined.postOptimize() // do post optimization
        mergeOptionMetadata(inlined.optionMetadata, other.optionMetadata) // merge option metadata
        inlined.inlineConfig = macroConfig
        return inlined
    }

    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: CwtConfigInlineMode): CwtPropertyConfig? {
        val inlined = CwtPropertyConfig.copy(
            sourceConfig = config,
            keyExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_KEY -> if (otherConfig is CwtPropertyConfig) otherConfig.keyExpression else return null
                CwtConfigInlineMode.VALUE_TO_KEY -> CwtDataExpression.resolve(otherConfig.value, CwtDataExpressionRole.Key)
                else -> config.keyExpression
            },
            valueExpression = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> if (otherConfig is CwtPropertyConfig) CwtDataExpression.resolve(otherConfig.key, CwtDataExpressionRole.Value) else return null
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueExpression
                else -> config.valueExpression
            },
            valueType = when (inlineMode) {
                CwtConfigInlineMode.VALUE_TO_VALUE -> otherConfig.valueType
                CwtConfigInlineMode.KEY_TO_VALUE -> CwtExpressionType.String
                else -> config.valueType
            },
            configs = when (inlineMode) {
                CwtConfigInlineMode.KEY_TO_VALUE -> null
                CwtConfigInlineMode.VALUE_TO_VALUE -> deepCopyConfigs(otherConfig)
                else -> deepCopyConfigs(config)
            },
        )
        inlined.postOptimize() // do post optimization
        mergeOptionMetadata(inlined.optionMetadata, config.optionMetadata) // merge option metadata
        inlined.withParentConfig(config.parentConfig)
        inlined.singleAliasConfig = config.singleAliasConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.inlineConfig = config.inlineConfig
        return inlined
    }

    fun inlineForConfigContext(config: CwtPropertyConfig, key: String): List<CwtMemberConfig<*>>? {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.AliasMatchLeft -> inlineAlias(config, key)
            CwtDataTypes.SingleAliasRight -> inlineSingleAlias(config)?.let { listOf(it) }
            else -> null
        }
    }

    fun inlineForConfig(config: CwtPropertyConfig): CwtPropertyConfig {
        // #76
        return inlineSingleAlias(config) ?: config
    }

    fun inlineForConfig(config: CwtMemberConfig<*>): CwtMemberConfig<*> {
        // #76
        if (config is CwtPropertyConfig) return inlineSingleAlias(config) ?: config
        return config
    }

    fun inlineForContextConfig(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        val inlined = CwtValueConfig.create(
            pointer = emptyPointer(),
            configGroup = configGroup,
            valueExpression = CwtDataExpression.resolveBlock(),
            valueType = CwtExpressionType.Block,
            configs = configs,
        )
        mergeOptionMetadata(inlined.optionMetadata, config?.optionMetadata) // merge option metadata
        return inlined
    }

    // endregion

    // region Expand Methods

    /**
     * 展开枚举规则 [config] 的所有作为候选项的值规则。
     */
    fun expandEnumCandidates(config: CwtEnumConfig, processor: (CwtValueConfig) -> Boolean): Boolean {
        if (config.valueConfigMap.isEmpty()) return true
        config.valueConfigMap.values.forEach { valueConfig ->
            val r = processor(valueConfig)
            if (!r) return false
        }
        return true
    }

    /**
     * 展开并集规则 [config] 的所有作为候选项的值规则。
     */
    fun expandUnionCandidates(config: CwtUnionConfig, processor: (CwtValueConfig) -> Boolean): Boolean {
        if (config.valueConfigs.isEmpty()) return true
        // NOTE 3.0.1 recursion guard should not be directly used here, since the context may be different
        config.valueConfigs.forEachFast { valueConfig ->
            val r = processor(valueConfig)
            if (!r) return false
        }
        return true
    }

    /**
     * 递归展开 [config] 的子规则中的所有形如 `subtype[{expression}] = {...}` 的属性规则中的子规则，保留其他形式的子规则。
     *
     * 结果序列中的元组的第一个元素是展开后的子规则，第二个元素是合并后的当前子类型表达式。
     */
    fun expandBySubtypeExpression(config: CwtMemberConfig<*>, processor: (CwtMemberConfig<*>, String) -> Boolean): Boolean {
        if (config.configs.isNullOrEmpty()) return true
        return doExpandBySubtypeExpression(config, "", processor)
    }

    private fun doExpandBySubtypeExpression(config: CwtMemberConfig<*>, currentExpression: String, processor: (CwtMemberConfig<*>, String) -> Boolean): Boolean {
        // NOTE 3.0.1 use processor pattern (instead of direct sequence builder) to optimize performance
        config.configs?.orNull()?.forEachFast { childConfig ->
            val nextExpression = extractSubtypeExpression(childConfig)
            if (nextExpression != null) {
                if (childConfig.configs?.orNull() != null) {
                    val mergedExpression = mergeSubtypeExpression(currentExpression, nextExpression)
                    doExpandBySubtypeExpression(childConfig, mergedExpression, processor).let { if (!it) return false }
                }
            } else {
                processor(childConfig, currentExpression).let { if (!it) return false }
            }
        }
        return true
    }

    fun expandConfigExpression(config: CwtConfig<*>, processor: (CwtDataExpression) -> Boolean): Boolean {
        return doExpandConfigExpression(config.configExpression, config.configGroup, processor)
    }

    fun expandConfigExpression(configs: Collection<CwtConfig<*>>, processor: (CwtDataExpression) -> Boolean): Boolean {
        if (configs.isEmpty()) return true
        return when (configs) {
            is List -> configs.processFast { config -> doExpandConfigExpression(config.configExpression, config.configGroup, processor) }
            else -> configs.process { config -> doExpandConfigExpression(config.configExpression, config.configGroup, processor) }
        }
    }

    fun expandKeyExpression(config: CwtPropertyConfig, processor: (CwtDataExpression) -> Boolean): Boolean {
        return doExpandConfigExpression(config.keyExpression, config.configGroup, processor)
    }

    fun expandKeyExpression(configs: Collection<CwtPropertyConfig>, processor: (CwtDataExpression) -> Boolean): Boolean {
        if (configs.isEmpty()) return true
        return when (configs) {
            is List -> configs.processFast { config -> doExpandConfigExpression(config.keyExpression, config.configGroup, processor) }
            else -> configs.process { config -> doExpandConfigExpression(config.keyExpression, config.configGroup, processor) }
        }
    }

    fun expandValueExpression(config: CwtMemberConfig<*>, processor: (CwtDataExpression) -> Boolean): Boolean {
        return doExpandConfigExpression(config.valueExpression, config.configGroup, processor)
    }

    fun expandValueExpression(configs: Collection<CwtMemberConfig<*>>, processor: (CwtDataExpression) -> Boolean): Boolean {
        if (configs.isEmpty()) return true
        return when (configs) {
            is List -> configs.processFast { config -> doExpandConfigExpression(config.valueExpression, config.configGroup, processor) }
            else -> configs.process { config -> doExpandConfigExpression(config.valueExpression, config.configGroup, processor) }
        }
    }

    private fun doExpandConfigExpression(configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, processor: (CwtDataExpression) -> Boolean): Boolean {
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

    // endregion
}
