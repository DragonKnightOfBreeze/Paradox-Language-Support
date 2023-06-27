package icu.windea.pls.lang

import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.cwt.expression.CwtDataType as T

object ParadoxConfigMergeHandler {
    fun mergeConfigs(configs: List<CwtMemberConfig<*>>, otherConfigs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        val result = mutableListOf<CwtMemberConfig<*>>()
        configs.forEach f1@{ config ->
            otherConfigs.forEach f2@{ otherConfig ->
                if(config is CwtPropertyConfig && otherConfig is CwtPropertyConfig) {
                    if(config.key.equals(otherConfig.key, true)) {
                        if(config.configs == null && otherConfig.configs == null) {
                            if(config.valueExpression == otherConfig.valueExpression) {
                                result.add(config)
                                return@f1
                            }
                        } else if(config.configs != null && otherConfig.configs != null) {
                            if(config.pointer == otherConfig.pointer) { //TODO
                                result.add(config)
                                return@f1
                            }
                        }
                    }
                } else if(config is CwtValueConfig && otherConfig is CwtValueConfig) {
                    if(config.configs == null && otherConfig.configs == null) {
                        if(config.valueExpression == otherConfig.valueExpression) {
                            result.add(config)
                            return@f1
                        }
                    } else if(config.configs != null && otherConfig.configs != null) {
                        if(config.pointer == otherConfig.pointer) { //TODO
                            result.add(config)
                            return@f1
                        }
                    }
                }
            }
        }
        for(config in result) {
            config.parent = null
        }
        return result
    }
    
    fun mergeValueConfig(c1: CwtValueConfig, c2: CwtValueConfig): CwtValueConfig? {
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
    
    private fun mergeDocumentations(c1: CwtDocumentationAware, c2: CwtDocumentationAware): String?{
        val d1 = c1.documentation?.takeIfNotEmpty()
        val d2 = c2.documentation?.takeIfNotEmpty()
        if(d1 == null || d2 == null) return d1 ?: d2
        if(d1 == d2) return d1
        return "$d1\n<br><br>\n$d2"
    }
}