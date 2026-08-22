@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.csv

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle

class QuoteLiteralIntention : PsiUpdateModCommandAction<ParadoxCsvExpressionElement>(ParadoxCsvExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.quoteLiteral")

    override fun invoke(context: ActionContext, element: CwtExpressionElement, updater: ModPsiUpdater) {
        val newText = element.text.unquote().quote() // unquote first
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxCsvExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxCsvColumn && element.canQuote()
    }
    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxCsvExpressionElement
    }
}

class UnquoteLiteralIntention : PsiUpdateModCommandAction<ParadoxCsvExpressionElement>(ParadoxCsvExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.unquoteLiteral")

    override fun invoke(context: ActionContext, element: CwtExpressionElement, updater: ModPsiUpdater) {
        val newText = element.text.unquote()
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxCsvExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxCsvColumn && element.canUnquote()
    }
    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxCsvExpressionElement
    }
}
