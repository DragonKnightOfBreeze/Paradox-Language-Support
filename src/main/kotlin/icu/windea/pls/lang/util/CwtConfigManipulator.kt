package icu.windea.pls.lang.util

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*

object CwtConfigManipulator {
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
    
    fun inlineAsValueConfig(config: CwtMemberConfig<*>?, configs: List<CwtMemberConfig<*>>?, configGroup: CwtConfigGroup): CwtValueConfig {
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = configGroup,
            value = PlsConstants.Folders.block,
            valueTypeId = CwtType.Block.id,
            configs = configs,
            options = config?.options,
            documentation = config?.documentation
        )
    }
    
    fun inlineSingleAliasOrAlias(element: PsiElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, matchOptions: Int = CwtConfigMatcher.Options.Default): List<CwtPropertyConfig> {
        //内联类型为single_alias_right或alias_match_left的规则
        val result = mutableListOf<CwtMemberConfig<*>>()
        run {
            val configGroup = config.configGroup
            val valueExpression = config.valueExpression
            when(valueExpression.type) {
                CwtDataTypes.SingleAliasRight -> {
                    val singleAliasName = valueExpression.value ?: return@run
                    val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@run
                    result.add(singleAlias.inline(config))
                }
                CwtDataTypes.AliasMatchLeft -> {
                    val aliasName = valueExpression.value ?: return@run
                    val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
                    val aliasSubNames = ParadoxExpressionHandler.getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchOptions)
                    aliasSubNames.forEach f1@{ aliasSubName ->
                        val aliases = aliasGroup[aliasSubName] ?: return@f1
                        aliases.forEach f2@{ alias ->
                            var inlinedConfig = alias.inline(config)
                            if(inlinedConfig.valueExpression.type == CwtDataTypes.SingleAliasRight) {
                                val singleAliasName = inlinedConfig.valueExpression.value ?: return@f2
                                val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@f2
                                inlinedConfig = singleAlias.inline(inlinedConfig)
                            }
                            result.add(inlinedConfig)
                        }
                    }
                }
            }
        }
        if(result.isEmpty()) return emptyList()
        val parentConfig = config.parentConfig
        if(parentConfig != null) {
            CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        }
        result.removeIf { it !is CwtPropertyConfig }
        return result.cast()
    }
    
    fun inlineSingleAlias(config: CwtPropertyConfig): CwtPropertyConfig? {
        //内联类型为single_alias_right的规则
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        when(valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> {
                val singleAliasName = valueExpression.value ?: return null
                val singleAlias = configGroup.singleAliases[singleAliasName] ?: return null
                val result = singleAlias.inline(config)
                return result
            }
            else -> return null
        }
    }

    //endregion
    
    //region Merge Methods
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
        if(c1.expression.type == CwtDataTypes.Block || c2.expression.type == CwtDataTypes.Block) return null //cannot merge non-same clauses
        val expressionString = mergeExpressionString(c1.expression, c2.expression)
        if(expressionString == null) return null
        return CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = c1.configGroup,
            value = c1.value,
            options = mergeOptions(c1, c2),
            documentation = mergeDocumentations(c1, c2)
        )
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
    
    private fun mergeOptions(c1: CwtOptionsAware, c2: CwtOptionsAware): List<CwtOptionMemberConfig<*>> {
        //keep duplicate options here (no affect to features)
        return merge(c1.options, c2.options)
    }
    
    private fun mergeDocumentations(c1: CwtDocumentationAware, c2: CwtDocumentationAware): String? {
        val d1 = c1.documentation?.orNull()
        val d2 = c2.documentation?.orNull()
        if(d1 == null || d2 == null) return d1 ?: d2
        if(d1 == d2) return d1
        return "$d1\n<br><br>\n$d2"
    }
    
    fun mergeAndMatchesValueConfig(configs: List<CwtValueConfig>, configExpression: CwtDataExpression): Boolean {
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
    //endregion
}
