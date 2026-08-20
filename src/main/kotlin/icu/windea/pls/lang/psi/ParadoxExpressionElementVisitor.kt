package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

abstract class ParadoxExpressionElementVisitor : PsiElementVisitor() {
    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element is ParadoxExpressionElement) visitExpressionElement(element)
    }

    open fun visitExpressionElement(element: ParadoxExpressionElement) {
        when (element) {
            is ParadoxScriptExpressionElement -> visitExpressionElement(element)
            is ParadoxLocalisationExpressionElement -> visitExpressionElement(element)
            is ParadoxCsvExpressionElement -> visitExpressionElement(element)
        }
    }

    open fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
        when (element) {
            is ParadoxScriptNumberExpressionElement -> visitNumberExpressionElement(element)
            is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
        }
    }

    open fun visitNumberExpressionElement(element: ParadoxScriptNumberExpressionElement) {

    }

    open fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {

    }

    open fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {

    }

    open fun visitExpressionElement(element: ParadoxCsvExpressionElement) {

    }
}
