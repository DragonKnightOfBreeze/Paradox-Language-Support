package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * @see ParadoxExpressionElement
 */
abstract class ParadoxPsiElementVisitor : PsiElementVisitor() {
    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element is ParadoxDefinitionElement) visitDefinitionElement(element)
        if (element is ParadoxExpressionElement) {
            visitExpressionElement(element)
            when (element) {
                is ParadoxScriptExpressionElement -> {
                    visitExpressionElement(element)
                    when (element) {
                        is ParadoxScriptNumberExpressionElement -> visitNumberExpressionElement(element)
                        is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
                    }
                }
                is ParadoxLocalisationExpressionElement -> {
                    visitExpressionElement(element)
                }
                is ParadoxCsvExpressionElement -> {
                    visitExpressionElement(element)
                }
            }
        }
        if (element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
        if (element is ParadoxConditionParameter) visitConditionParameter(element)
        if (element is ParadoxParameter) visitParameter(element)
        if (element is ParadoxLocalisationParameter) visitLocalisationParameter(element)
    }

    open fun visitDefinitionElement(element: ParadoxDefinitionElement) {

    }

    open fun visitExpressionElement(element: ParadoxExpressionElement) {

    }

    open fun visitExpressionElement(element: ParadoxScriptExpressionElement) {

    }

    open fun visitNumberExpressionElement(element: ParadoxScriptNumberExpressionElement) {

    }

    open fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {

    }

    open fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {

    }

    open fun visitExpressionElement(element: ParadoxCsvExpressionElement) {

    }

    open fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {

    }

    open fun visitConditionParameter(element: ParadoxConditionParameter) {

    }

    open fun visitParameter(element: ParadoxParameter) {

    }

    open fun visitLocalisationParameter(element: ParadoxLocalisationParameter) {

    }
}
