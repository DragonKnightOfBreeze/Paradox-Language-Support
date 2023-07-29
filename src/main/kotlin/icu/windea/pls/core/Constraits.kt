package icu.windea.pls.core

import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

/**
 * 用于优化本地化查询。
 */
enum class ParadoxLocalisationConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val predicate: (String) -> Boolean,
    val ignoreCase: Boolean = false,
) {
    Default(ParadoxLocalisationNameIndexKey, { true }),
    Modifier(ParadoxLocalisationNameIndexModifierKey, { it.startsWith("mod_", true) }, ignoreCase = true);
    
    companion object {
        val values = values()
    }
}

enum class ParadoxResolveConstraint {
    ScriptedVariable {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptedVariableReference -> true
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptedVariablePsiReference -> true
                else -> false
            }
        }
    },
    Definition {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxScriptInt -> true
                is ParadoxLocalisationCommandField -> true //<scripted_loc>
                is ParadoxLocalisationConceptName -> true //<game_concept>
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type == CwtDataType.Definition
                }
                is ParadoxTemplateSnippetExpressionReference -> {
                    val configExpression = reference.configExpression
                    configExpression.type == CwtDataType.Definition
                }
                is ParadoxDataExpressionNode.Reference -> {
                    reference.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.expression ?: return@any false
                        configExpression.type == CwtDataType.Definition
                    }
                }
                is ParadoxLocalisationCommandFieldPsiReference -> true //<scripted_loc>
                is ParadoxLocalisationConceptNamePsiReference -> true //<game_concept>
                else -> false
            }
        }
    },
    Localisation {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationPropertyReference -> true
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type == CwtDataType.Localisation || configExpression.type == CwtDataType.InlineLocalisation
                }
                is ParadoxLocalisationPropertyPsiReference -> true
                else -> false
            }
        }
    },
    Parameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxParameter -> true
                is ParadoxConditionParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type == CwtDataType.Parameter
                }
                is ParadoxScriptValueArgumentExpressionNode.Reference -> true
                is ParadoxParameterPsiReference -> true
                is ParadoxConditionParameterPsiReference -> true
                else -> false
            }
        }
    },
    LocalisationParameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxLocalisationPropertyReference -> true
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type == CwtDataType.LocalisationParameter
                }
                is ParadoxLocalisationPropertyPsiReference -> true
                else -> false
            }
        }
    },
    ValueSetValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationCommandScope -> true
                is ParadoxLocalisationCommandField -> true
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type.isValueSetValueType()
                }
                is ParadoxTemplateSnippetExpressionReference -> {
                    val configExpression = reference.configExpression
                    configExpression.type.isValueSetValueType()
                }
                is ParadoxDataExpressionNode.Reference -> {
                    reference.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.expression ?: return@any false
                        configExpression.type.isValueSetValueType()
                    }
                }
                is ParadoxValueSetValueExpressionNode.Reference -> true
                is ParadoxLocalisationCommandScopePsiReference -> true //value[event_target], value[global_event_target]
                is ParadoxLocalisationCommandFieldPsiReference -> true //value[variable]
                else -> false
            }
        }
    },
    ComplexEnumValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type == CwtDataType.EnumValue
                }
                is ParadoxTemplateSnippetExpressionReference -> {
                    val configExpression = reference.configExpression
                    configExpression.type == CwtDataType.EnumValue
                }
                is ParadoxDataExpressionNode.Reference -> {
                    reference.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.expression ?: return@any false
                        configExpression.type == CwtDataType.EnumValue
                    }
                }
                is ParadoxComplexEnumValuePsiReference -> true
                else -> false
            }
        }
    },
    ;
    
    open fun canResolveReference(element: PsiElement): Boolean = true
    
    open fun canResolve(reference: PsiReference): Boolean = true
}

fun PsiElement.canResolveReference(constraint: ParadoxResolveConstraint) = constraint.canResolveReference(this)

fun PsiReference.canResolve(constraint: ParadoxResolveConstraint) = constraint.canResolve(this)