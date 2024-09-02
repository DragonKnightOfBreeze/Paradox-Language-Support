package icu.windea.pls.model.constraints

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

enum class ParadoxResolveConstraint {
    ScriptedVariable {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
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
                is ParadoxLocalisationExpressionElement -> element.isComplexExpression()
                is ParadoxScriptInt -> element.isExpression()
                is ParadoxLocalisationIcon -> true
                is ParadoxLocalisationConcept -> true //<game_concept>
                is ParadoxLocalisationColorfulText -> true //<text_color>
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type == CwtDataTypes.Definition || configExpression.type == CwtDataTypes.TechnologyWithLevel
                }
                is ParadoxTemplateSnippetExpressionReference -> {
                    val configExpression = reference.configExpression
                    configExpression.type == CwtDataTypes.Definition
                }
                is ParadoxDataSourceNode.Reference -> {
                    reference.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.expression ?: return@any false
                        configExpression.type == CwtDataTypes.Definition
                    }
                }
                is ParadoxScriptValueNode.Reference -> true //<script_value>
                is ParadoxDynamicCommandFieldNode.Reference -> true //<scripted_loc>
                is ParadoxDatabaseObjectNode.Reference -> true
                is ParadoxLocalisationIconPsiReference -> true
                is ParadoxLocalisationConceptPsiReference -> true //<game_concept>
                is ParadoxLocalisationColorPsiReference -> true //<text_color>
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
                    configExpression.type == CwtDataTypes.Localisation || configExpression.type == CwtDataTypes.InlineLocalisation
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
                    configExpression.type == CwtDataTypes.Parameter
                }
                is ParadoxScriptValueArgumentNode.Reference -> true
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
                    configExpression.type == CwtDataTypes.LocalisationParameter
                }
                is ParadoxLocalisationPropertyPsiReference -> true
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
                    configExpression.type == CwtDataTypes.EnumValue
                }
                is ParadoxTemplateSnippetExpressionReference -> {
                    val configExpression = reference.configExpression
                    configExpression.type == CwtDataTypes.EnumValue
                }
                is ParadoxDataSourceNode.Reference -> {
                    reference.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.expression ?: return@any false
                        configExpression.type == CwtDataTypes.EnumValue
                    }
                }
                is ParadoxComplexEnumValuePsiReference -> true
                else -> false
            }
        }
    },
    DynamicValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isCommandExpression()
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type in CwtDataTypeGroups.DynamicValue
                }
                is ParadoxTemplateSnippetExpressionReference -> {
                    val configExpression = reference.configExpression
                    configExpression.type in CwtDataTypeGroups.DynamicValue
                }
                is ParadoxDataSourceNode.Reference -> {
                    reference.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.expression ?: return@any false
                        configExpression.type in CwtDataTypeGroups.DynamicValue
                    }
                }
                is ParadoxDynamicValueNode.Reference -> true
                is ParadoxDynamicCommandFieldNode.Reference -> true //value[variable]
                else -> false
            }
        }
    },
    DynamicValueStrictly {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when(element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isCommandExpression()
                else -> false
            }
        }
        
        override fun canResolve(reference: PsiReference): Boolean {
            return when(reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.expression ?: return false
                    configExpression.type in CwtDataTypeGroups.DynamicValue
                }
                is ParadoxDynamicValueNode.Reference -> true
                is ParadoxDynamicCommandFieldNode.Reference -> true //value[variable]
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
