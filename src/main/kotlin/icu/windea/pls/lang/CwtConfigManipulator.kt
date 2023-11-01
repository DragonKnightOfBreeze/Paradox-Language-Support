package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import java.util.function.*

object CwtConfigManipulator {
    //region Deep Copy Methods
    fun deepCopyConfigs(config: CwtMemberConfig<*>): List<CwtMemberConfig<*>>? {
        val cs1 = config.configs
        if(cs1.isNullOrEmpty()) return cs1
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEachFast f1@{ c1 ->
            result += c1.delegated(deepCopyConfigs(c1), c1.parentConfig)
        }
        return result
    }
    
    fun deepCopyConfigsInDeclarationConfig(
        config: CwtMemberConfig<*>,
        context: CwtDeclarationConfigContext,
        action: BiConsumer<MutableList<CwtMemberConfig<*>>, CwtMemberConfig<*>>? = null
    ): List<CwtMemberConfig<*>>? {
        val cs1 = config.configs
        if(cs1.isNullOrEmpty()) return cs1
        val result = mutableListOf<CwtMemberConfig<*>>()
        cs1.forEachFast f1@{ c1 ->
            if(c1 is CwtPropertyConfig) {
                val subtypeExpression = c1.key.removeSurroundingOrNull("subtype[", "]")
                if(subtypeExpression != null) {
                    val subtypes = context.definitionSubtypes
                    if(subtypes == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                        val cs2 = deepCopyConfigsInDeclarationConfig(c1, context, action)
                        if(cs2.isNullOrEmpty()) return@f1
                        result += cs2
                    }
                    return@f1
                }
            }
            
            if(action == null) {
                result += c1.delegated(deepCopyConfigsInDeclarationConfig(c1, context), c1.parentConfig)
            } else {
                action.accept(result, c1)
            }
        }
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
        inlined.configs?.forEachFast { it.parentConfig = inlined }
        inlined.parentConfig = config.parentConfig
        inlined.inlineableConfig = config.inlineableConfig
        return inlined
    }
    
    fun inlineByConfig(element: PsiElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, matchOptions: Int = CwtConfigMatcher.Options.Default): List<CwtMemberConfig<*>> {
        //内联类型为single_alias_right或alias_match_left的规则
        val configGroup = config.info.configGroup
        val valueExpression = config.valueExpression
        when(valueExpression.type) {
            CwtDataType.SingleAliasRight -> {
                val singleAliasName = valueExpression.value ?: return emptyList()
                val singleAlias = configGroup.singleAliases[singleAliasName] ?: return emptyList()
                val result = mutableListOf<CwtMemberConfig<*>>()
                result.add(singleAlias.inline(config))
                return result
            }
            CwtDataType.AliasMatchLeft -> {
                val aliasName = valueExpression.value ?: return emptyList()
                val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
                val result = mutableListOf<CwtMemberConfig<*>>()
                val aliasSubNames = CwtConfigHandler.getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchOptions)
                aliasSubNames.forEachFast f1@{ aliasSubName ->
                    val aliases = aliasGroup[aliasSubName] ?: return@f1
                    aliases.forEachFast f2@{ alias ->
                        var inlinedConfig = alias.inline(config)
                        if(inlinedConfig.valueExpression.type == CwtDataType.SingleAliasRight) {
                            val singleAliasName = inlinedConfig.valueExpression.value ?: return@f2
                            val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@f2
                            inlinedConfig = singleAlias.inline(inlinedConfig)
                        }
                        result.add(inlinedConfig)
                    }
                }
                return result
            }
            else -> return emptyList()
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
        cs1.forEachFast f1@{ config ->
            cs2.forEachFast f2@{ otherConfig ->
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
        if(c1.expression.type == CwtDataType.Block || c2.expression.type == CwtDataType.Block) return null //cannot merge non-same clauses
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
        val ignoreCase = e1.type == CwtDataType.Constant && e2.type == CwtDataType.Constant
        if(e1.expressionString.equals(e2.expressionString, ignoreCase)) return e1.expressionString
        return doMergeExpressionString(e1, e2) ?: doMergeExpressionString(e2, e1)
    }
    
    private fun doMergeExpressionString(e1: CwtDataExpression, e2: CwtDataExpression): String? {
        val t1 = e1.type
        val t2 = e2.type
        return when(t1) {
            CwtDataType.Any -> e2.expressionString
            CwtDataType.Int -> when(t2) {
                CwtDataType.Int, CwtDataType.Float, CwtDataType.ValueField, CwtDataType.IntValueField, CwtDataType.VariableField, CwtDataType.IntVariableField -> "int"
                else -> null
            }
            CwtDataType.Float -> when(t2) {
                CwtDataType.Float, CwtDataType.ValueField, CwtDataType.VariableField -> "float"
                else -> null
            }
            CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> when(t2) {
                CwtDataType.ScopeField -> e1.expressionString
                CwtDataType.Scope -> if(e2.value == "any") e1.expressionString else null
                else -> null
            }
            CwtDataType.Value, CwtDataType.ValueSet, CwtDataType.ValueOrValueSet -> when(t2) {
                CwtDataType.Value, CwtDataType.ValueSet, CwtDataType.ValueOrValueSet -> if(e1.value == e2.value) "value_or_value_set[${e1.value}]" else null
                CwtDataType.ValueField, CwtDataType.IntValueField, CwtDataType.VariableField, CwtDataType.IntVariableField -> "value_or_value_set[${e1.value}]"
                else -> null
            }
            CwtDataType.VariableField -> when(t2) {
                CwtDataType.VariableField, CwtDataType.ValueField -> "variable_field"
                else -> null
            }
            CwtDataType.IntVariableField -> when(t2) {
                CwtDataType.IntVariableField, CwtDataType.ValueField, CwtDataType.IntValueField -> "int_variable_field"
                else -> null
            }
            CwtDataType.IntValueField -> when(t2) {
                CwtDataType.ValueField, CwtDataType.IntValueField -> "int_value_field"
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
    //endregion
}
