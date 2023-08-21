package icu.windea.pls.lang

import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.cwt.expression.CwtDataType as T

object ParadoxConfigMerger {
    fun mergeConfigs(cs1: List<CwtMemberConfig<*>>, cs2: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        //try to merge single value configs first (by value expressions)
        val c1 = cs1.singleOrNull()
        val c2 = cs2.singleOrNull()
        if(c1 is CwtValueConfig && c2 is CwtValueConfig) {
            val resultConfig = mergeValueConfig(c1, c2)
            if(resultConfig != null) return resultConfig.toSingletonList()
        }
        
        //merge multiple configs
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEach f1@{ config ->
            cs2.forEach f2@{ otherConfig ->
                val resultConfig = mergeConfig(config, otherConfig)
                if(resultConfig != null) result.add(resultConfig)
            }
        }
        for(config in result) {
            config.parent = null
        }
        return result
    }
    
    fun mergeConfig(c1: CwtMemberConfig<*>, c2: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if(c1 === c2) return c1 //reference equality
        if(c1.pointer == c2.pointer) return c1 //value equality (should be)
        val ic1 = c1.inlineableConfig
        val ic2 = c2.inlineableConfig
        if(ic1 != null && ic2 != null) {
            if(ic1.pointer == ic2.pointer) {
                //value equality after inline (should be)
                return when(c1) {
                    is CwtPropertyConfig -> c1.copy(
                        pointer = emptyPointer(),
                        options = mergeOptions(c1, c2),
                        documentation = mergeDocumentations(c1, c2)
                    )
                    is CwtValueConfig -> c1.copy(
                        pointer = emptyPointer(),
                        options = mergeOptions(c1, c2),
                        documentation = mergeDocumentations(c1, c2)
                    )
                }
            }
        }
        return null
    }
    
    fun mergeValueConfig(c1: CwtValueConfig, c2: CwtValueConfig): CwtValueConfig? {
        if(c1 === c2) return c1 //reference equality
        if(c1.pointer == c2.pointer) return c1 //value equality (should be) 
        if(c1.expression.type == T.Block || c2.expression.type == T.Block) return null //cannot merge non-same clauses
        val expressionString = mergeExpressionString(c1.expression, c2.expression)
        if(expressionString == null) return null
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            info = c1.info,
            value = c1.value,
            options = mergeOptions(c1, c2),
            documentation = mergeDocumentations(c1, c2)
        )
    }
    
    fun mergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        val ignoreCase = e1.type == T.Constant && e2.type == T.Constant
        if(e1.expressionString.equals(e2.expressionString, ignoreCase)) return e1.expressionString
        return doMergeExpressionString(e1, e2) ?: doMergeExpressionString(e2, e1)
    }
    
    private fun doMergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        val t1 = e1.type
        val t2 = e2.type
        return when(t1) {
            T.Any -> e2.expressionString
            T.Int -> when(t2) {
                T.Int, T.Float, T.ValueField, T.IntValueField, T.VariableField, T.IntVariableField -> "int"
                else -> null
            }
            T.Float -> when(t2) {
                T.Float, T.ValueField, T.VariableField -> "float"
                else -> null
            }
            T.ScopeField, T.Scope, T.ScopeGroup -> when(t2) {
                T.ScopeField -> e1.expressionString
                T.Scope -> if(e2.value == "any") e1.expressionString else null
                else -> null
            }
            T.Value, T.ValueSet, T.ValueOrValueSet -> when(t2) {
                T.Value, T.ValueSet, T.ValueOrValueSet -> if(e1.value == e2.value) "value_or_value_set[${e1.value}]" else null
                T.ValueField, T.IntValueField, T.VariableField, T.IntVariableField -> "value_or_value_set[${e1.value}]"
                else -> null
            }
            T.VariableField -> when(t2) {
                T.VariableField, T.ValueField -> "variable_field"
                else -> null
            }
            T.IntVariableField -> when(t2) {
                T.IntVariableField, T.ValueField, T.IntValueField -> "int_variable_field"
                else -> null
            }
            T.IntValueField -> when(t2) {
                T.ValueField, T.IntValueField -> "int_value_field"
                else -> null
            }
            else -> null
        }
    }
    
    private fun mergeOptions(c1: CwtOptionsAware, c2: CwtOptionsAware): List<CwtOptionMemberConfig<*>> {
        //keep duplicate options here (no affect to features)
        return merge(c1.options, c2.options)
    }
    
    private fun mergeDocumentations(c1: CwtDocumentationAware, c2: CwtDocumentationAware): String? {
        val d1 = c1.documentation?.takeIfNotEmpty()
        val d2 = c2.documentation?.takeIfNotEmpty()
        if(d1 == null || d2 == null) return d1 ?: d2
        if(d1 == d2) return d1
        return "$d1\n<br><br>\n$d2"
    }
}