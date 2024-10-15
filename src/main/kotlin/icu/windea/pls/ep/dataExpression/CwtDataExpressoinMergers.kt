package icu.windea.pls.ep.dataExpression

import icu.windea.pls.config.expression.*
import icu.windea.pls.config.CwtDataTypeGroups as TypeGroups
import icu.windea.pls.config.CwtDataTypes as Types

class DefaultCwtDataExpressionMerger : CwtDataExpressionMerger {
    override fun merge(expression: CwtDataExpression, otherExpression: CwtDataExpression): String? {
        return mergeExpressionString(expression, otherExpression)
    }

    private fun mergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        if (e1.type == Types.Constant && e2.type == Types.Constant) {
            if (e1.expressionString.equals(e2.expressionString, true)) return e1.expressionString.lowercase()
        }
        return doMergeExpressionString(e1, e2) ?: doMergeExpressionString(e2, e1)
    }

    private fun doMergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        when {
            e1.type == Types.Any -> {
                return e2.expressionString
            }
            e1.type == Types.Scalar -> when {
                e2.type == Types.Block -> return null
                e2.type == Types.ColorField -> return null
                else -> return e2.expressionString
            }
            e1.type == Types.Int -> when {
                e2.type == Types.Float -> return "int"
                e2.type == Types.ValueField || e2.type == Types.VariableField -> return "int"
                e2.type == Types.IntValueField || e2.type == Types.IntVariableField -> return "int"
            }
            e1.type == Types.Float -> when {
                e2.type == Types.ValueField || e2.type == Types.VariableField -> return "float"
            }
            e1.type in TypeGroups.ScopeField -> when {
                e2.type == Types.ScopeField -> return e1.expressionString
                e2.type == Types.Scope && e2.value == null -> return e1.expressionString
            }
            e1.type in TypeGroups.DynamicValue -> when {
                e2.type in TypeGroups.DynamicValue -> return if (e1.value == e2.value) "dynamic_value[${e1.value}]" else null
                e2.type in TypeGroups.ValueField -> return "dynamic_value[${e1.value}]"
                e2.type in TypeGroups.VariableField -> return if (e1.value == "variable") "dynamic_value[${e1.value}]" else null
            }
            e1.type == Types.VariableField -> when {
                e2.type in TypeGroups.ValueField -> return "variable_field"
            }
            e1.type == Types.IntVariableField -> when {
                e2.type in TypeGroups.ValueField -> return "int_variable_field"
            }
            e1.type == Types.IntValueField -> when {
                e2.type == Types.ValueField -> return "int_value_field"
            }
        }
        return null
    }
}
