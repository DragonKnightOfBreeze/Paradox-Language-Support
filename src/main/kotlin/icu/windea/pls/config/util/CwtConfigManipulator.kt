package icu.windea.pls.config.util

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.model.*

object CwtConfigManipulator {
    fun getDistinctKey(config: CwtMemberConfig<*>): String {
        return when(config) {
            is CwtPropertyConfig -> {
                when {
                    config.configs == null -> "${config.key}=${config.value}"
                    config.configs.isNullOrEmpty() -> "${config.key}={}"
                    else -> {
                        val v = config.configs!!.joinToString("\u0000") { getDistinctKey(it) }
                        return "${config.key}={${v}}"
                    }
                }
            }
            is CwtValueConfig -> {
                when {
                    config.configs == null -> config.value
                    config.configs.isNullOrEmpty() -> "{}"
                    else -> {
                        val v = config.configs!!.joinToString("\u0000") { getDistinctKey(it) }
                        return "{${v}}"
                    }
                }
            }
        }
    }
    
    //region Deep Copy Methods
    fun deepCopyConfigs(config: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = config): List<CwtMemberConfig<*>>? {
        val cs1 = config.configs
        if(cs1.isNullOrEmpty()) return cs1
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEach f1@{ c1 ->
            result += c1.delegated(deepCopyConfigs(c1), parentConfig)
        }
        CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        return result
    }
    
    fun deepCopyConfigsInDeclarationConfig(config: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*> = config, context: CwtDeclarationConfigContext): List<CwtMemberConfig<*>>? {
        val cs1 = config.configs
        if(cs1.isNullOrEmpty()) return cs1
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEach f1@{ c1 ->
            if(c1 is CwtPropertyConfig) {
                val subtypeExpression = c1.key.removeSurroundingOrNull("subtype[", "]")
                if(subtypeExpression != null) {
                    val subtypes = context.definitionSubtypes
                    if(subtypes == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                        val cs2 = deepCopyConfigsInDeclarationConfig(c1, parentConfig, context)
                        if(cs2.isNullOrEmpty()) return@f1
                        result += cs2
                    }
                    return@f1
                }
            }
            
            result += c1.delegated(deepCopyConfigsInDeclarationConfig(c1, parentConfig, context), parentConfig)
        }
        CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        return result
    }
    //endregion
    
    //region Inline Methods
    enum class InlineMode {
        KEY_TO_KEY, KEY_TO_VALUE, VALUE_TO_KEY, VALUE_TO_VALUE
    }
    
    fun inlineWithConfig(config: CwtPropertyConfig, otherConfig: CwtMemberConfig<*>, inlineMode: InlineMode): CwtPropertyConfig? {
        val inlined = config.copy(
            key = when(inlineMode) {
                InlineMode.KEY_TO_KEY -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                InlineMode.VALUE_TO_KEY -> otherConfig.value
                else -> config.key
            },
            value = when(inlineMode) {
                InlineMode.VALUE_TO_VALUE -> otherConfig.value
                InlineMode.KEY_TO_VALUE -> if(otherConfig is CwtPropertyConfig) otherConfig.key else return null
                else -> config.value
            },
            configs = when(inlineMode) {
                InlineMode.KEY_TO_VALUE -> null
                InlineMode.VALUE_TO_VALUE -> deepCopyConfigs(otherConfig)
                else -> deepCopyConfigs(config)
            },
        )
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.inlineableConfig = config.inlineableConfig
        return inlined
    }
    
    fun inlineWithConfigs(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = configGroup,
            value = PlsConstants.Folders.block,
            valueType = CwtType.Block,
            configs = configs,
            optionConfigs = config?.optionConfigs,
            documentation = config?.documentation
        )
    }
    
    fun <T : CwtMemberConfig<*>> inlineSingleAlias(config: T): T? {
        if(config !is CwtPropertyConfig) return null
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        if(valueExpression.type != CwtDataTypes.SingleAliasRight) return null
        val singleAliasName = valueExpression.value ?: return null
        val singleAliasConfig = configGroup.singleAliases[singleAliasName] ?: return null
        @Suppress("UNCHECKED_CAST")
        return singleAliasConfig.inline(config) as T
    }
    //endregion
    
    //region Merge Methods
    /**
     * @param relax 如果为true，最终合并得到的结果应当能匹配每一个规则列表。
     */
    fun mergeConfigsNew(configs: List<CwtMemberConfig<*>>, otherConfigs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        if(configs.isEmpty() && otherConfigs.isEmpty()) return emptyList()
        if(configs.isEmpty()) return otherConfigs
        if(otherConfigs.isEmpty()) return configs
        
        if(configs.size == 1 && otherConfigs.size == 1) {
            val c1 = configs.single()
            val c2 = otherConfigs.single()
            if(c1 is CwtValueConfig && c2 is CwtValueConfig) {
                val merged = mergeValueConfig(c1, c2)
                if(merged != null) return merged.toSingletonList()
            } else if(c1 is CwtPropertyConfig && c2 is CwtPropertyConfig) {
                val same = getDistinctKey(c1) == getDistinctKey(c2)
                if(same) return c1.toSingletonList()
            } else {
                return emptyList()
            }
        }
        
        val m1 = configs.associateBy { getDistinctKey(it) }
        val m2 = otherConfigs.associateBy { getDistinctKey(it) }
        val allKeys = m1.keys union m2.keys
        val sameKeys = m1.keys intersect m2.keys
        val sameConfigs = sameKeys.mapNotNull { m1[it] ?: m2[it] }
        if(sameKeys.size == allKeys.size) return sameConfigs
        
        return merge(configs, otherConfigs).distinctBy { getDistinctKey(it) }
    }
    
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
            config.parentConfig = null
        }
        return result
    }
    
    fun mergeConfig(c1: CwtMemberConfig<*>, c2: CwtMemberConfig<*>): CwtMemberConfig<*>? {
        if(c1 === c2) return c1 //reference equality
        if(c1.pointer == c2.pointer) return c1 //value equality (should be)
        val ic1 = c1.inlineableConfig
        val ic2 = c2.inlineableConfig
        if(ic1 != null && ic2 != null) {
            if(ic1.config.pointer == ic2.config.pointer) {
                //value equality after inline (should be)
                return when(c1) {
                    is CwtPropertyConfig -> c1.copy(
                        pointer = emptyPointer(),
                        optionConfigs = mergeOptions(c1.optionConfigs, c2.optionConfigs),
                        documentation = mergeDocumentations(c1.documentation, c2.documentation)
                    )
                    is CwtValueConfig -> c1.copy(
                        pointer = emptyPointer(),
                        optionConfigs = mergeOptions(c1.optionConfigs, c2.optionConfigs),
                        documentation = mergeDocumentations(c1.documentation, c2.documentation)
                    )
                }
            }
        }
        return null
    }
    
    fun mergeValueConfig(c1: CwtValueConfig, c2: CwtValueConfig): CwtValueConfig? {
        if(c1 === c2) return c1 //reference equality
        if(c1.pointer == c2.pointer) return c1 //value equality (should be) 
        if(c1.expression.type == CwtDataTypes.Block || c2.expression.type == CwtDataTypes.Block) return null //cannot merge non-same clauses
        val expressionString = mergeExpressionString(c1.expression, c2.expression)
        if(expressionString == null) return null
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = c1.configGroup,
            value = c1.value,
            optionConfigs = mergeOptions(c1.optionConfigs, c2.optionConfigs),
            documentation = mergeDocumentations(c1.documentation, c2.documentation)
        )
    }
    
    fun mergeAndMatchValueConfig(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
        if(configs.isEmpty()) return false
        for(config in configs) {
            val e1 = configExpression //expect
            val e2 = config.expression //actual (e.g., from parameterized key)
            val e3 = mergeExpressionString(e1, e2) ?: continue //merged
            
            //"scope_field" merge "scope[country]" -> "scope[country]" -> true
            
            //TODO 1.3.8+ optimize merge & match logic
            
            if(e3 == e2.expressionString) return true
        }
        return false
    }
    
    fun mergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        val ignoreCase = e1.type == CwtDataTypes.Constant && e2.type == CwtDataTypes.Constant
        if(e1.expressionString.equals(e2.expressionString, ignoreCase)) return e1.expressionString
        return doMergeExpressionString(e1, e2) ?: doMergeExpressionString(e2, e1)
    }
    
    private fun doMergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        val t1 = e1.type
        val t2 = e2.type
        return when(t1) {
            CwtDataTypes.Any -> e2.expressionString
            CwtDataTypes.Int -> when(t2) {
                CwtDataTypes.Int, CwtDataTypes.Float, CwtDataTypes.ValueField, CwtDataTypes.IntValueField, CwtDataTypes.VariableField, CwtDataTypes.IntVariableField -> "int"
                else -> null
            }
            CwtDataTypes.Float -> when(t2) {
                CwtDataTypes.Float, CwtDataTypes.ValueField, CwtDataTypes.VariableField -> "float"
                else -> null
            }
            CwtDataTypes.Scalar -> when(t2) {
                CwtDataTypes.Block, CwtDataTypes.ColorField -> null
                else -> "scalar"
            }
            CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> when(t2) {
                CwtDataTypes.ScopeField -> e1.expressionString
                CwtDataTypes.Scope -> if(e2.value == "any") e1.expressionString else null
                else -> null
            }
            CwtDataTypes.Value, CwtDataTypes.ValueSet, CwtDataTypes.DynamicValue -> when(t2) {
                CwtDataTypes.Value, CwtDataTypes.ValueSet, CwtDataTypes.DynamicValue -> if(e1.value == e2.value) "dynamic_value[${e1.value}]" else null
                CwtDataTypes.ValueField, CwtDataTypes.IntValueField, CwtDataTypes.VariableField, CwtDataTypes.IntVariableField -> "dynamic_value[${e1.value}]"
                else -> null
            }
            CwtDataTypes.VariableField -> when(t2) {
                CwtDataTypes.VariableField, CwtDataTypes.ValueField -> "variable_field"
                else -> null
            }
            CwtDataTypes.IntVariableField -> when(t2) {
                CwtDataTypes.IntVariableField, CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> "int_variable_field"
                else -> null
            }
            CwtDataTypes.IntValueField -> when(t2) {
                CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> "int_value_field"
                else -> null
            }
            else -> null
        }
    }
    
    private fun mergeOptions(a: List<CwtOptionMemberConfig<*>>?, b: List<CwtOptionMemberConfig<*>>?): List<CwtOptionMemberConfig<*>> {
        //keep duplicate options here (no affect to features)
        return merge(a, b)
    }
    
    private fun mergeDocumentations(a: String?, b: String?): String? {
        val d1 = a?.orNull()
        val d2 = b?.orNull()
        if(d1 == null || d2 == null) return d1 ?: d2
        if(d1 == d2) return d1
        return "$d1\n<br><br>\n$d2"
    }
    //endregion
}
