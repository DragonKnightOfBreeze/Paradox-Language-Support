package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.equalsFast

@Optimized
object CwtConfigExpressionManipulationService {
    // region Merge Methods

    fun mergeDataExpression(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        val type = expression.type
        val otherType = otherExpression.type
        val expressionString = expression.expressionString
        val otherExpressionString = otherExpression.expressionString
        // cannot merge block data expressions here (no further info)
        if (type == CwtDataTypes.Block || otherType == CwtDataTypes.Block) return null
        // check whether expression strings are same
        if (expressionString.equalsFast(otherExpressionString)) return expressionString
        // check whether expression strings are same (ignore case) for constant data expressions
        if (type == CwtDataTypes.Constant && otherType == CwtDataTypes.Constant) {
            if (expressionString.equalsFast(otherExpressionString, ignoreCase = true)) return expressionString.lowercase()
        }
        if (type == CwtDataTypes.Constant || otherType == CwtDataTypes.Constant) return null
        // apply detailed merge logic
        return mergeDataExpressionRemain(expression, otherExpression)
    }

    private fun mergeDataExpressionRemain(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        return mergeDataExpressionDirectional(expression, otherExpression) ?: mergeDataExpressionDirectional(otherExpression, expression)
    }

    private fun mergeDataExpressionDirectional(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        val type = expression.type
        val otherType = otherExpression.type
        val expressionString = expression.expressionString
        val otherExpressionString = otherExpression.expressionString
        val value = expression.metadata.value
        val otherValue = otherExpression.metadata.value
        when (type) {
            CwtDataTypes.Any -> return otherExpressionString
            CwtDataTypes.Scalar -> when {
                otherType == CwtDataTypes.ColorField -> return null
                otherType == CwtDataTypes.Scalar -> {
                    if (expression.metadata.wildcard && otherExpression.metadata.wildcard) return "wildcard_scalar"
                    return "scalar"
                }
                else -> return otherExpressionString
            }
            CwtDataTypes.Int -> when {
                otherType == CwtDataTypes.Float -> return "int"
                otherType == CwtDataTypes.ValueField || otherType == CwtDataTypes.VariableField -> return "int"
                otherType == CwtDataTypes.IntValueField || otherType == CwtDataTypes.IntVariableField -> return "int"
            }
            CwtDataTypes.Float -> when {
                otherType == CwtDataTypes.ValueField || otherType == CwtDataTypes.VariableField -> return "float"
            }
            CwtDataTypes.IntPercentageField -> when {
                otherType == CwtDataTypes.PercentageField -> return "int_percentage_field"
            }
            in CwtDataTypeSets.DynamicValue -> {
                when {
                    otherType in CwtDataTypeSets.DynamicValue -> {
                        if (value != null && value.equalsFast(otherValue)) return "dynamic_value[$value]"
                    }
                    otherType in CwtDataTypeSets.ValueField -> {
                        if (value != null) return "dynamic_value[$value]"
                    }
                    otherType in CwtDataTypeSets.VariableField -> {
                        if (value.equalsFast("variable")) return "dynamic_value[$value]"
                    }
                }
            }
            in CwtDataTypeSets.ScopeField -> {
                when {
                    otherType == CwtDataTypes.ScopeField -> return expressionString
                    otherType == CwtDataTypes.Scope && otherValue == null -> return expressionString
                }
            }
            CwtDataTypes.VariableField -> when {
                otherType in CwtDataTypeSets.ValueField -> return "variable_field"
            }
            CwtDataTypes.IntVariableField -> when {
                otherType in CwtDataTypeSets.ValueField -> return "int_variable_field"
            }
            CwtDataTypes.IntValueField -> when {
                otherType == CwtDataTypes.ValueField -> return "int_value_field"
            }
        }
        return null
    }

    // endregion
}
