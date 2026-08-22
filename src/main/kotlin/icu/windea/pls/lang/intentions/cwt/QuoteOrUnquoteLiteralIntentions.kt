@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.cwt

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtNumberExpressionElement
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle

class QuoteLiteralIntention : PsiUpdateModCommandAction<CwtExpressionElement>(CwtExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.quoteLiteral")

    override fun invoke(context: ActionContext, element: CwtExpressionElement, updater: ModPsiUpdater) {
        val newText = element.text.unquote().quote() // unquote first
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: CwtExpressionElement, context: ActionContext): Boolean {
        // can also be applied to number literals
        if (element is CwtNumberExpressionElement) return true
        return element is CwtStringExpressionElement && element.canQuote()
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is CwtExpressionElement
    }
}

class UnquoteLiteralIntention : PsiUpdateModCommandAction<CwtExpressionElement>(CwtExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.unquoteLiteral")

    override fun invoke(context: ActionContext, element: CwtExpressionElement, updater: ModPsiUpdater) {
        val newText = element.text.unquote()
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: CwtExpressionElement, context: ActionContext): Boolean {
        return element is CwtStringExpressionElement && element.canUnquote()
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is CwtExpressionElement
    }
}
