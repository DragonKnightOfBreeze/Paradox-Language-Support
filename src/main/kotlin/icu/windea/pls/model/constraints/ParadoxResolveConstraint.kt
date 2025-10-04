package icu.windea.pls.model.constraints

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.csv.ParadoxCsvExpressionPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationConceptPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextColorPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextFormatPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextIconPsiReference
import icu.windea.pls.lang.references.script.ParadoxComplexEnumValuePsiReference
import icu.windea.pls.lang.references.script.ParadoxConditionParameterPsiReference
import icu.windea.pls.lang.references.script.ParadoxParameterPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxIdentifierNode
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextColorAwareElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import icu.windea.pls.script.psi.isResolvableExpression

enum class ParadoxResolveConstraint {
    ScriptedVariable {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptedVariableReference -> true
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptedVariablePsiReference -> true
                else -> false
            }
        }
    },
    Definition {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptExpressionElement -> element.isResolvableExpression() && element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isComplexExpression()
                is ParadoxLocalisationIcon -> true // <sprite>, etc.
                is ParadoxLocalisationConceptCommand -> true // <game_concept>
                is ParadoxLocalisationTextColorAwareElement -> true // <text_color>
                is ParadoxLocalisationTextFormat -> true // <text_format>
                is ParadoxLocalisationTextIcon -> true // <text_icon>
                is ParadoxCsvColumn -> !element.isHeaderColumn()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.DefinitionAware || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxLocalisationIconPsiReference -> true // <sprite>, etc.
                is ParadoxLocalisationConceptPsiReference -> true // <game_concept>
                is ParadoxLocalisationTextColorPsiReference -> true // <text_color>
                is ParadoxLocalisationTextFormatPsiReference -> true // <text_format>
                is ParadoxLocalisationTextIconPsiReference -> true // <text_icon>
                is ParadoxCsvExpressionPsiReference -> {
                    val configExpression = reference.columnConfig.valueConfig?.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.Definition
                }
                else -> false
            }
        }
    },
    Localisation {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isDatabaseObjectExpression(strict = true)
                is ParadoxLocalisationParameter -> true
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.LocalisationAware || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxLocalisationParameterPsiReference -> true
                else -> false
            }
        }
    },
    Parameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxParameter -> true
                is ParadoxConditionParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.Parameter
                }
                is ParadoxParameterPsiReference -> true
                is ParadoxConditionParameterPsiReference -> true
                else -> false
            }
        }
    },
    LocalisationParameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxLocalisationParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.LocalisationParameter
                }
                is ParadoxLocalisationParameterPsiReference -> true
                else -> false
            }
        }
    },
    ComplexEnumValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxCsvColumn -> !element.isHeaderColumn()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.EnumValue || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxComplexEnumValuePsiReference -> true
                is ParadoxCsvExpressionPsiReference -> {
                    val configExpression = reference.columnConfig.valueConfig?.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.EnumValue
                }
                else -> false
            }
        }
    },
    DynamicValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isCommandExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.DynamicValue || dataType == CwtDataTypes.AliasKeysField
                }
                else -> false
            }
        }
    },
    DynamicValueStrictly {
        override fun canResolveReference(element: PsiElement): Boolean {
            return DynamicValue.canResolveReference(element)
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxIdentifierNode.Reference -> reference.canResolveFor(this)
                else -> DynamicValue.canResolve(reference)
            }
        }
    },
    ;

    open fun canResolveReference(element: PsiElement): Boolean = true

    open fun canResolve(reference: PsiReference): Boolean = true
}
