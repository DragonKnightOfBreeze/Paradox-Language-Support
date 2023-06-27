package icu.windea.pls.lang

import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*

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
    
    fun shallowMergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        if(config.expression.type == CwtDataType.Block || otherConfig.expression.type == CwtDataType.Block) {
            return null
        }
        if(config.expression == otherConfig.expression) {
            return config.copy(propertyConfig = null)
        }
        return doShallowMergeValueConfig(config, otherConfig) ?: doShallowMergeValueConfig(otherConfig, config)
    }
    
    private fun doShallowMergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        val e1 = config.expression
        val e2 = otherConfig.expression
        val t1 = e1.type
        val t2 = e2.type
        return when(t1) {
            CwtDataType.Int -> when(t2) {
                CwtDataType.Int, CwtDataType.Float, CwtDataType.ValueField, CwtDataType.IntValueField, CwtDataType.VariableField, CwtDataType.IntVariableField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, "int")
                }
                else -> null
            }
            CwtDataType.Float -> when(t2) {
                CwtDataType.Float, CwtDataType.ValueField, CwtDataType.VariableField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, "float")
                }
                else -> null
            }
            CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> when(t2) {
                CwtDataType.ScopeField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, config.value)
                }
                CwtDataType.Scope -> {
                    if(e2.value == "any") CwtValueConfig.resolve(config.pointer, config.info, config.value) else null
                }
                else -> null
            }
            CwtDataType.Value, CwtDataType.ValueSet, CwtDataType.ValueOrValueSet -> when(t2) {
                CwtDataType.Value, CwtDataType.ValueSet, CwtDataType.ValueOrValueSet -> {
                    if(e1.value == e2.value) CwtValueConfig.resolve(config.pointer, config.info, "value_or_value_set[${e1.value}]") else null
                }
                CwtDataType.ValueField, CwtDataType.IntValueField, CwtDataType.VariableField, CwtDataType.IntVariableField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, "value_or_value_set[${e1.value}]")
                }
                else -> null
            }
            CwtDataType.VariableField -> when(t2) {
                CwtDataType.VariableField, CwtDataType.ValueField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, "variable_field")
                }
                else -> null
            }
            CwtDataType.IntVariableField -> when(t2) {
                CwtDataType.IntVariableField, CwtDataType.ValueField, CwtDataType.IntValueField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, "int_variable_field")
                }
                else -> null
            }
            CwtDataType.IntValueField -> when(t2) {
                CwtDataType.ValueField, CwtDataType.IntValueField -> {
                    CwtValueConfig.resolve(config.pointer, config.info, "int_value_field")
                }
                else -> null
            }
            else -> null
        }
    }
}