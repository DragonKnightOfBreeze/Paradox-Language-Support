package icu.windea.pls.config

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.CwtDataType as T


object CwtConfigMerger {
    fun mergeValueConfig(c1: CwtValueConfig, c2: CwtValueConfig): CwtValueConfig? {
        if(c1.expression == c2.expression) return c1
        return doMergeValueConfig(c1, c2) ?: doMergeValueConfig(c2, c1)
    }
    
    fun doMergeValueConfig(c1: CwtValueConfig, c2: CwtValueConfig): CwtValueConfig? {
        val t1 = c1.expression.type
        val t2 = c2.expression.type
        return when(t1) {
            T.Int -> {
                when(t2) {
                    T.Int, T.Float, T.ValueField, T.IntValueField, T.VariableField, T.IntVariableField -> CwtValueConfig(c1.pointer, c1.info, "int")
                    else -> null
                }
            }
            T.Float -> {
                when(t2) {
                    T.Float, T.ValueField, T.VariableField ->CwtValueConfig(c1.pointer, c1.info, "float")
                    else -> null
                }
            }
            T.ScopeField, T.Scope, T.ScopeGroup -> {
                when {
                    t2 == T.ScopeField -> CwtValueConfig(c1.pointer, c1.info, c1.value)
                    t2 == T.Scope && c2.expression.value == "any" -> CwtValueConfig(c1.pointer, c1.info, c1.value)
                    else -> null
                }
            }
            T.VariableField -> {
                when(t2) {
                    T.VariableField, T.ValueField -> CwtValueConfig(c1.pointer, c1.info, "variable_field")
                    else -> null
                }
            }
            T.IntVariableField -> {
                when(t2) {
                    T.IntVariableField, T.ValueField, T.IntValueField -> CwtValueConfig(c1.pointer, c1.info, "int_variable_field")
                    else -> null
                }
            }
            T.IntValueField -> {
                when(t2) {
                    T.ValueField, T.IntValueField -> CwtValueConfig(c1.pointer, c1.info, "int_value_field")
                    else -> null
                }
            }
            else -> null
        }
    }
}