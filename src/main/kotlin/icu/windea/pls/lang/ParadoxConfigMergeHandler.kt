package icu.windea.pls.lang

import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*

object ParadoxConfigMergeHandler {
    fun mergeConfigs(configs: List<CwtMemberConfig<*>>, otherConfigs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        return emptyList() //TODO
    }
    
    fun mergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        if(config.expression == otherConfig.expression) return config
        return doMergeValueConfig(config, otherConfig) ?: doMergeValueConfig(otherConfig, config)
    }
    
    private fun doMergeValueConfig(config: CwtValueConfig, otherConfig: CwtValueConfig): CwtValueConfig? {
        val e1 = config.expression
        val e2 = otherConfig.expression
        val t1 = e1.type
        val t2 = e2.type
        return when(t1) {
            CwtDataType.Int -> {
                when(t2) {
                    CwtDataType.Int, CwtDataType.Float, CwtDataType.ValueField, CwtDataType.IntValueField, CwtDataType.VariableField, CwtDataType.IntVariableField -> CwtValueConfig.resolve(config.pointer, config.info, "int")
                    else -> null
                }
            }
            CwtDataType.Float -> {
                when(t2) {
                    CwtDataType.Float, CwtDataType.ValueField, CwtDataType.VariableField -> CwtValueConfig.resolve(config.pointer, config.info, "float")
                    else -> null
                }
            }
            CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
                when(t2) {
                    CwtDataType.ScopeField -> CwtValueConfig.resolve(config.pointer, config.info, config.value)
                    CwtDataType.Scope -> if(e2.value == "any") CwtValueConfig.resolve(config.pointer, config.info, config.value) else null
                    else -> null
                }
            }
            CwtDataType.Value, CwtDataType.ValueSet, CwtDataType.ValueOrValueSet -> {
                when(t2) {
                    CwtDataType.Value, CwtDataType.ValueSet, CwtDataType.ValueOrValueSet -> if(e1.value == e2.value) CwtValueConfig.resolve(config.pointer, config.info, "value_or_value_set[${e1.value}]") else null
                    CwtDataType.ValueField, CwtDataType.IntValueField -> CwtValueConfig.resolve(config.pointer, config.info, "value_or_value_set[${e1.value}]")
                    CwtDataType.VariableField, CwtDataType.IntVariableField -> CwtValueConfig.resolve(config.pointer, config.info, "value_or_value_set[${e1.value}]")
                    else -> null
                }
            }
            CwtDataType.VariableField -> {
                when(t2) {
                    CwtDataType.VariableField, CwtDataType.ValueField -> CwtValueConfig.resolve(config.pointer, config.info, "variable_field")
                    else -> null
                }
            }
            CwtDataType.IntVariableField -> {
                when(t2) {
                    CwtDataType.IntVariableField, CwtDataType.ValueField, CwtDataType.IntValueField -> CwtValueConfig.resolve(config.pointer, config.info, "int_variable_field")
                    else -> null
                }
            }
            CwtDataType.IntValueField -> {
                when(t2) {
                    CwtDataType.ValueField, CwtDataType.IntValueField -> CwtValueConfig.resolve(config.pointer, config.info, "int_value_field")
                    else -> null
                }
            }
            else -> null
        }
    }
}