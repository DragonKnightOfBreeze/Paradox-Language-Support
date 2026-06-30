package icu.windea.pls.config.manipulation

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression

object CwtConfigExpressionManipulationService {
    // region Merge Methods

    fun mergeDataExpression(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        if (expression.type == CwtDataTypes.Constant && otherExpression.type == CwtDataTypes.Constant) {
            return when {
                expression.expressionString == otherExpression.expressionString -> expression.expressionString
                expression.expressionString.equals(otherExpression.expressionString, true) -> expression.expressionString.lowercase()
                else -> null
            }
        }
        if (expression.type == CwtDataTypes.Constant || otherExpression.type == CwtDataTypes.Constant) {
            return null
        }
        return mergeDataExpressionBidirectional(expression, otherExpression)
    }

    private fun mergeDataExpressionBidirectional(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        return mergeDataExpressionDirectional(expression, otherExpression) ?: mergeDataExpressionDirectional(otherExpression, expression)
    }

    private fun mergeDataExpressionDirectional(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        when (expression.type) {
            CwtDataTypes.Any -> {
                return otherExpression.expressionString
            }
            CwtDataTypes.Scalar -> when {
                otherExpression.type == CwtDataTypes.Block -> return null
                otherExpression.type == CwtDataTypes.ColorField -> return null
                else -> return otherExpression.expressionString
            }
            CwtDataTypes.Int -> when {
                otherExpression.type == CwtDataTypes.Float -> return "int"
                otherExpression.type == CwtDataTypes.ValueField || otherExpression.type == CwtDataTypes.VariableField -> return "int"
                otherExpression.type == CwtDataTypes.IntValueField || otherExpression.type == CwtDataTypes.IntVariableField -> return "int"
            }
            CwtDataTypes.Float -> when {
                otherExpression.type == CwtDataTypes.ValueField || otherExpression.type == CwtDataTypes.VariableField -> return "float"
            }
            CwtDataTypes.IntPercentageField -> when {
                otherExpression.type == CwtDataTypes.PercentageField -> return "int_percentage_field"
            }
            in CwtDataTypeSets.DynamicValue -> when {
                otherExpression.type in CwtDataTypeSets.DynamicValue -> if (expression.value != null && expression.value == otherExpression.value) return "dynamic_value[${expression.value}]"
                otherExpression.type in CwtDataTypeSets.ValueField -> if (expression.value != null) return "dynamic_value[${expression.value}]"
                otherExpression.type in CwtDataTypeSets.VariableField -> if (expression.value == "variable") return "dynamic_value[${expression.value}]"
            }
            in CwtDataTypeSets.ScopeField -> when {
                otherExpression.type == CwtDataTypes.ScopeField -> return expression.expressionString
                otherExpression.type == CwtDataTypes.Scope && otherExpression.value == null -> return expression.expressionString
            }
            CwtDataTypes.VariableField -> when {
                otherExpression.type in CwtDataTypeSets.ValueField -> return "variable_field"
            }
            CwtDataTypes.IntVariableField -> when {
                otherExpression.type in CwtDataTypeSets.ValueField -> return "int_variable_field"
            }
            CwtDataTypes.IntValueField -> when {
                otherExpression.type == CwtDataTypes.ValueField -> return "int_value_field"
            }
        }
        return null
    }

    // endregion
}
