package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.expandUnionCandidates
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.equalsFast
import icu.windea.pls.core.withRecursionGuard

@Optimized
object CwtConfigExpressionManipulationService {
    // region Merge Methods

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

    private fun mergeDataExpressionRemain(expression: CwtDataExpression, otherExpression: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        mergeDataExpressionDirectional(expression, otherExpression, configGroup)?.let { return it }
        mergeDataExpressionDirectional(otherExpression, expression, configGroup)?.let { return it }
        return null
    }

    private fun mergeDataExpressionDirectional(expression: CwtDataExpression, otherDataExpression: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        val dataType = expression.type
        val otherDataType = otherDataExpression.type
        val expressionString = expression.expressionString
        val otherExpressionString = otherDataExpression.expressionString
        when (dataType) {
            CwtDataTypes.Any -> return otherExpressionString
            CwtDataTypes.Scalar -> when (otherDataType) {
                CwtDataTypes.ColorField -> return null
                CwtDataTypes.Scalar -> {
                    if (expression.metadata.wildcard && otherDataExpression.metadata.wildcard) return "wildcard_scalar"
                    return "scalar"
                }
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
                    val name = expression.metadata.value
                    val otherName = otherDataExpression.metadata.value
                    if (name != null && name.equalsFast(otherName)) return "dynamic_value[$name]"
                }
                in CwtDataTypeSets.ValueField -> {
                    val name = expression.metadata.value
                    if (name != null) return "dynamic_value[$name]"
                }
                in CwtDataTypeSets.VariableField -> {
                    val name = expression.metadata.value
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
            CwtDataTypes.UnionValue -> {
                val name = expression.metadata.value ?: return null
                val unionConfig = configGroup.unions[name] ?: return null
                // NOTE 3.0.1 recursion guard is required here
                withRecursionGuard("CwtConfigExpressionManipulationService.mergeDataExpression") {
                    unionConfig.expandUnionCandidates { valueConfig ->
                        val e = valueConfig.configExpression
                        withRecursionCheck(e) {
                            mergeDataExpressionDirectional(e, otherDataExpression, configGroup)
                        }
                        true
                    }
                }
            }
            CwtDataTypes.AliasKeysField, CwtDataTypes.AliasName -> {
                val name = expression.metadata.value ?: return null
                val aliasConfigGroup = configGroup.aliasGroups[name] ?: return null
                // NOTE 3.0.1 recursion guard is required here
                withRecursionGuard("CwtConfigExpressionManipulationService.mergeDataExpression") {
                    withRecursionCheck(name) {
                        for (aliasConfigs in aliasConfigGroup.values) {
                            val e = aliasConfigs.firstOrNull()?.configExpression ?: continue
                            withRecursionCheck(e) {
                                mergeDataExpressionDirectional(e, otherDataExpression, configGroup)
                            }
                        }
                    }
                }
            }
            CwtDataTypes.SingleAliasRight -> {
                val name = expression.metadata.value ?: return null
                val singleAliasConfig = configGroup.singleAliases[name] ?: return null
                // NOTE 3.0.1 recursion guard is required here
                withRecursionGuard("CwtConfigExpressionManipulationService.mergeDataExpression") {
                    val e = singleAliasConfig.config.valueExpression
                    withRecursionCheck(e) {
                        mergeDataExpressionDirectional(e, otherDataExpression, configGroup)
                    }
                }
            }
        }
        return null
    }

    // endregion
}
