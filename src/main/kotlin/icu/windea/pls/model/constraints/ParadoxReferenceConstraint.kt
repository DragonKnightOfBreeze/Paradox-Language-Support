package icu.windea.pls.model.constraints

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.isCommandExpression
import icu.windea.pls.lang.psi.isComplexExpression
import icu.windea.pls.lang.psi.isDatabaseObjectExpression
import icu.windea.pls.lang.psi.isResolvableLiteralExpression
import icu.windea.pls.lang.references.ParadoxComplexEnumValuePsiReference
import icu.windea.pls.lang.references.ParadoxConstrainedPsiReference
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationConceptPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextColorPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextFormatPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextIconPsiReference
import icu.windea.pls.lang.references.script.ParadoxConditionParameterPsiReference
import icu.windea.pls.lang.references.script.ParadoxDefinitionInjectionTargetPsiReference
import icu.windea.pls.lang.references.script.ParadoxEventNamespacePsiReference
import icu.windea.pls.lang.references.script.ParadoxParameterPsiReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextColorAwareElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

/**
 * 引用约束。
 *
 * 用于测试指定的 [PsiElement] 是否可以解析得到特定的 [PsiReference]，而指定的 [PsiReference] 又是否可以解析为特定的 [PsiElement]。
 *
 * 解析约束可以用来优化性能，另一方面，也会影响一些语言功能（如内嵌提示）的可用性。
 */
enum class ParadoxReferenceConstraint {
    ScriptedVariable {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptedVariableReference -> true
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptedVariablePsiReference -> true
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return false // unavailable
        }
    },

    Definition {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptExpressionElement -> element.isResolvableLiteralExpression() && element.isDataExpression()
                is ParadoxLocalisationExpressionElement -> element.isComplexExpression()
                is ParadoxLocalisationIcon -> true // <sprite>, etc.
                is ParadoxLocalisationConceptCommand -> true // <game_concept>
                is ParadoxLocalisationTextColorAwareElement -> true // <text_color>
                is ParadoxLocalisationTextIcon -> true // <text_icon>
                is ParadoxLocalisationTextFormat -> true // <text_format>
                is ParadoxCsvColumn -> !ParadoxCsvPsiService.isHeaderColumn(element)
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                is ParadoxLocalisationTextColorPsiReference -> true // <text_color>
                is ParadoxLocalisationIconPsiReference -> true // <sprite>, etc.
                is ParadoxLocalisationConceptPsiReference -> true // <game_concept>
                is ParadoxLocalisationTextIconPsiReference -> true // <text_icon>
                is ParadoxLocalisationTextFormatPsiReference -> true // <text_format>
                is ParadoxEventNamespacePsiReference -> true // <event_namespace>
                is ParadoxDefinitionInjectionTargetPsiReference -> true
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType in CwtDataTypeSets.DefinitionAware || dataType in CwtDataTypeSets.Expandable
        }
    },

    Localisation {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isDataExpression()
                is ParadoxLocalisationExpressionElement -> element.isDatabaseObjectExpression(strict = true)
                is ParadoxLocalisationParameter -> true
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                is ParadoxLocalisationParameterPsiReference -> true
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType in CwtDataTypeSets.LocalisationAware || dataType in CwtDataTypeSets.Expandable
        }
    },

    ComplexEnumValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isDataExpression()
                is ParadoxCsvColumn -> !ParadoxCsvPsiService.isHeaderColumn(element)
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                is ParadoxComplexEnumValuePsiReference -> true
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.EnumValue || dataType in CwtDataTypeSets.Expandable
        }
    },

    DynamicValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isDataExpression()
                is ParadoxLocalisationExpressionElement -> element.isCommandExpression()
                is ParadoxCsvColumn -> !ParadoxCsvPsiService.isHeaderColumn(element)
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType in CwtDataTypeSets.DynamicValue || dataType in CwtDataTypeSets.Expandable
        }
    },

    Parameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxParameter -> true
                is ParadoxConditionParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isDataExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                is ParadoxParameterPsiReference -> true
                is ParadoxConditionParameterPsiReference -> true
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Parameter || dataType in CwtDataTypeSets.Expandable
        }
    },

    LocalisationParameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxLocalisationParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isDataExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                is ParadoxLocalisationParameterPsiReference -> true
                else -> false
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.LocalisationParameter || dataType in CwtDataTypeSets.Expandable
        }
    },

    LocalisationReference {
        override fun canResolveReference(element: PsiElement): Boolean {
            return Localisation.canResolveReference(element)
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                else -> Localisation.canResolve(reference)
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            // also for synced_localisation
            return dataType in CwtDataTypeSets.LocalisationReference || dataType in CwtDataTypeSets.Expandable
        }
    },

    DynamicValueReference {
        override fun canResolveReference(element: PsiElement): Boolean {
            return DynamicValue.canResolveReference(element)
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxConstrainedPsiReference -> reference.canResolveFor(this)
                else -> DynamicValue.canResolve(reference)
            }
        }

        override fun test(dataType: CwtDataType): Boolean {
            return dataType in CwtDataTypeSets.DynamicValue || dataType in CwtDataTypeSets.Expandable
        }
    },

    ;

    open fun canResolveReference(element: PsiElement): Boolean = true

    open fun canResolve(reference: PsiReference): Boolean = true

    open fun test(dataType: CwtDataType): Boolean = true
}
