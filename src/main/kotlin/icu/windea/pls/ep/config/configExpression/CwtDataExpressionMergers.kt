package icu.windea.pls.ep.config.configExpression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

class CwtBaseDataExpressionMerger : CwtDataExpressionMerger {
    override fun merge(configExpression1: CwtDataExpression, configExpression2: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        return mergeExpressionString(configExpression1, configExpression2, configGroup)
    }

    private fun mergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        when {
            e1.type == CwtDataTypes.Constant && e2.type == CwtDataTypes.Constant && e1.expressionString.equals(e2.expressionString, true) -> {
                return e1.expressionString.lowercase()
            }
            e1.type == CwtDataTypes.Constant || e2.type == CwtDataTypes.Constant -> {
                return null
            }
            else -> {
                return mergeExpressionStringTo(e1, e2, configGroup) ?: mergeExpressionStringTo(e2, e1, configGroup)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun mergeExpressionStringTo(e1: CwtDataExpression, e2: CwtDataExpression, configGroup: CwtConfigGroup): String? {
        when (e1.type) {
            CwtDataTypes.Any -> {
                return e2.expressionString
            }
            CwtDataTypes.Scalar -> when {
                e2.type == CwtDataTypes.Block -> return null
                e2.type == CwtDataTypes.ColorField -> return null
                else -> return e2.expressionString
            }
            CwtDataTypes.Int -> when {
                e2.type == CwtDataTypes.Float -> return "int"
                e2.type == CwtDataTypes.ValueField || e2.type == CwtDataTypes.VariableField -> return "int"
                e2.type == CwtDataTypes.IntValueField || e2.type == CwtDataTypes.IntVariableField -> return "int"
            }
            CwtDataTypes.Float -> when {
                e2.type == CwtDataTypes.ValueField || e2.type == CwtDataTypes.VariableField -> return "float"
            }
            in CwtDataTypeSets.ScopeField -> when {
                e2.type == CwtDataTypes.ScopeField -> return e1.expressionString
                e2.type == CwtDataTypes.Scope && e2.value == null -> return e1.expressionString
            }
            in CwtDataTypeSets.DynamicValue -> when {
                e2.type in CwtDataTypeSets.DynamicValue -> return if (e1.value != null && e1.value == e2.value) "dynamic_value[${e1.value}]" else null
                e2.type in CwtDataTypeSets.ValueField -> return if (e1.value != null) "dynamic_value[${e1.value}]" else null
                e2.type in CwtDataTypeSets.VariableField -> return if (e1.value == "variable") "dynamic_value[${e1.value}]" else null
            }
            CwtDataTypes.VariableField -> when {
                e2.type in CwtDataTypeSets.ValueField -> return "variable_field"
            }
            CwtDataTypes.IntVariableField -> when {
                e2.type in CwtDataTypeSets.ValueField -> return "int_variable_field"
            }
            CwtDataTypes.IntValueField -> when {
                e2.type == CwtDataTypes.ValueField -> return "int_value_field"
            }
        }
        return null
    }
}
