package icu.windea.pls.config.manipulation

import com.intellij.psi.PsiElement
import com.intellij.util.SmartList
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.isSamePointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.manipulation.CwtConfigInlineService.inlineForContextConfig
import icu.windea.pls.config.option.CwtOptionMetadata
import icu.windea.pls.config.util.CwtConfigKeyManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.allFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.equalsFast
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.model.type.CwtExpressionType

@Optimized
object CwtConfigManipulationService {
    // region Common Methods

    fun createListForDeepCopy(): MutableList<CwtMemberConfig<*>> {
        return SmartList() // 3.0.1 optimize: use `SmartList` here (reduce temporary memory overhead, especially for the sizes of 0 and 1)
    }

    fun createListForDeepCopy(expectedSize: Int): MutableList<CwtMemberConfig<*>> {
        require(expectedSize >= 0) { "expectedSize must be non-negative" }
        if (expectedSize <= 1) return SmartList() // 3.0.1 optimize: use `SmartList` here (reduce temporary memory overhead, especially for the sizes of 0 and 1)
        return ArrayList(expectedSize) // 3.0.1 optimize: use sized mutable list here
    }

    /**
     * 递归拷贝 [parentConfig] 中的所有子节点，并加入作为 [containerConfig] 的子规则。
     */
    fun deepCopyConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*> = parentConfig): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigs(parentConfig, containerConfig)
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

    /**
     * 递归拷贝 [parentConfig] 中的所有子节点，并加入作为 [containerConfig] 的子规则。
     *
     * 在这之前，首先会展开 [parentConfig] 的子规则中的所有形如 `subtype[{expression}] = {...}` 的属性规则中的子规则。
     *
     * @param subtypes 作为候选项的一组子类型。如果为 `null`（不是空列表），则会直接跳过展开逻辑。
     */
    fun deepCopyConfigsBySubtypeExpression(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*> = parentConfig, subtypes: List<String>?): List<CwtMemberConfig<*>>? {
        return doDeepCopyConfigsInDeclaration(parentConfig, containerConfig, subtypes)
    }

    private fun doDeepCopyConfigsInDeclaration(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, subtypes: List<String>?): List<CwtMemberConfig<*>>? {
        val configs = parentConfig.configs?.optimized() ?: return null // 这里需要兼容并同样处理子规则列表为空的情况
        if (configs.isEmpty()) return configs
        val result = createListForDeepCopy(/* expectedSize = configs.size */)
        configs.forEachFast f@{ config ->
            run r@{
                // 如果匹配子类型表达式，打平其中的子规则并加入结果，否则直接跳过
                if (subtypes == null) return@r
                val subtypeExpression = extractSubtypeExpression(config) ?: return@r
                if (config.configs.isNullOrEmpty()) return@f // skip
                val matched = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)
                if (!matched) return@f // skip
                result += doDeepCopyConfigsInDeclaration(config, containerConfig, subtypes).orEmpty()
                return@f
            }

            val childConfigs = config.configs
            val childResult = if (childConfigs != null) createListForDeepCopy(/* expectedSize = childConfigs.size */) else null
            val delegatedConfig = config.delegated(childResult).also { it.withParentConfig(containerConfig) }
            if (childResult != null) childResult += doDeepCopyConfigsInDeclaration(config, delegatedConfig, subtypes).orEmpty()
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

    fun skipMergedConfigs(mergedConfigs: List<CwtMemberConfig<*>>): Boolean {
        // 3.0.2 skip for empty merged result
        if (mergedConfigs.isEmpty()) return true
        if (mergedConfigs.size == 1) {
            val c = mergedConfigs.single()
            if (c.valueType == CwtExpressionType.Block) {
                // 3.0.2 skip for empty container config specially
                // otherwise, `UnresolvedExpressionInspection` will cause false positives in injected contexts, since the context configs are not empty (will be `{}` in such situation)
                if (c.configs.isNullOrEmpty()) return true
            }
        }
        return false
    }

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
        val expressionString = mergeDataExpression(config.configExpression, otherConfig.configExpression, config.configGroup)
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

    fun mergeAndMatchValueConfigs(configs: List<CwtValueConfig>, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        configs.forEachFast f@{ config ->
            val e1 = configExpression // expect
            val e2 = config.configExpression // actual (e.g., from parameterized key)
            val e3 = mergeDataExpression(e1, e2, configGroup) ?: return@f // merged
            if (e3 == e2.expressionString) return true
        }
        return false
    }

    fun mergeDataExpression(dataExpression: CwtDataExpression, otherDataExpression: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        val dataType = dataExpression.type
        val otherDataType = otherDataExpression.type
        val expressionString = dataExpression.expressionString
        val otherExpressionString = otherDataExpression.expressionString
        // cannot merge block data expressions here (no further info)
        if (dataType == CwtDataTypes.Block || otherDataType == CwtDataTypes.Block) return null
        // check whether expression strings are same
        if (expressionString.equalsFast(otherExpressionString)) return expressionString
        // check whether expression strings are same (ignore case) for constant data expressions
        if (dataType == CwtDataTypes.Constant && otherDataType == CwtDataTypes.Constant) {
            if (expressionString.equalsFast(otherExpressionString, ignoreCase = true)) return expressionString.lowercase()
        }
        if (dataType == CwtDataTypes.Constant || otherDataType == CwtDataTypes.Constant) return null
        // apply detailed merge logic
        return mergeDataExpressionRemain(dataExpression, otherDataExpression, configGroup)
    }

    private fun mergeDataExpressionRemain(dataExpression: CwtDataExpression, otherExpression: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        mergeDataExpressionDirectional(dataExpression, otherExpression, configGroup)?.let { return it }
        mergeDataExpressionDirectional(otherExpression, dataExpression, configGroup)?.let { return it }
        return null
    }

    private fun mergeDataExpressionDirectional(dataExpression: CwtDataExpression, otherDataExpression: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        val dataType = dataExpression.type
        val otherDataType = otherDataExpression.type
        val expressionString = dataExpression.expressionString
        val otherExpressionString = otherDataExpression.expressionString
        when (dataType) {
            CwtDataTypes.Any -> return otherExpressionString
            CwtDataTypes.Literal -> when (otherDataType) {
                CwtDataTypes.ColorField -> return null
                else -> return otherExpressionString
            }
            CwtDataTypes.Scalar -> when (otherDataType) {
                CwtDataTypes.ColorField -> return null
                else -> return otherExpressionString
            }
            CwtDataTypes.Int -> when (otherDataType) {
                CwtDataTypes.Float -> return "int"
                CwtDataTypes.ValueField, CwtDataTypes.VariableField -> return "int"
                CwtDataTypes.IntValueField, CwtDataTypes.IntVariableField -> return "int"
            }
            CwtDataTypes.Float -> when (otherDataType) {
                CwtDataTypes.ValueField -> return "float"
                CwtDataTypes.VariableField -> return "float"
            }
            CwtDataTypes.IntPercentageField -> when (otherDataType) {
                CwtDataTypes.PercentageField -> return "int_percentage_field"
            }
            in CwtDataTypeSets.DynamicValue -> when (otherDataType) {
                in CwtDataTypeSets.DynamicValue -> {
                    val name = dataExpression.metadata.value
                    val otherName = otherDataExpression.metadata.value
                    if (name != null && name.equalsFast(otherName)) return "dynamic_value[$name]"
                }
                in CwtDataTypeSets.ValueField -> {
                    val name = dataExpression.metadata.value
                    if (name != null) return "dynamic_value[$name]"
                }
                in CwtDataTypeSets.VariableField -> {
                    val name = dataExpression.metadata.value
                    if (name.equalsFast("variable")) return "dynamic_value[$name]"
                }
            }
            in CwtDataTypeSets.ScopeField -> when (otherDataType) {
                CwtDataTypes.ScopeField -> return expressionString
                CwtDataTypes.Scope -> {
                    val otherName = otherDataExpression.metadata.value
                    if (otherName == null) return expressionString
                }
            }
            CwtDataTypes.VariableField -> when (otherDataType) {
                in CwtDataTypeSets.ValueField -> return "variable_field"
            }
            CwtDataTypes.IntVariableField -> when (otherDataType) {
                in CwtDataTypeSets.ValueField -> return "int_variable_field"
            }
            CwtDataTypes.IntValueField -> when (otherDataType) {
                CwtDataTypes.ValueField -> return "int_value_field"
            }
            in CwtDataTypeSets.Expandable -> {
                // NOTE 3.0.3 recursion guard is required here
                CwtConfigExpansionService.expandExpandable(dataExpression, configGroup, "configExpression.mergeDataExpression") { e ->
                    mergeDataExpressionDirectional(e, otherDataExpression, configGroup)
                    true
                }
            }
        }
        return null
    }
}
