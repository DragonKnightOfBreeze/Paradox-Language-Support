@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.csv

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.isQuoted
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement

class QuoteIdentifierIntention : PsiUpdateModCommandAction<ParadoxCsvExpressionElement>(ParadoxCsvExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.quoteIdentifier")

    override fun invoke(context: ActionContext, element: ParadoxCsvExpressionElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.quote())
    }

    override fun isElementApplicable(element: ParadoxCsvExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxCsvColumn && canQuote(element)
    }

    private fun canQuote(element: ParadoxCsvExpressionElement): Boolean {
        val text = element.text
        return !text.isQuoted()
    }
}

class UnquoteIdentifierIntention : PsiUpdateModCommandAction<ParadoxCsvExpressionElement>(ParadoxCsvExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.unquoteIdentifier")

    override fun invoke(context: ActionContext, element: ParadoxCsvExpressionElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }

    override fun isElementApplicable(element: ParadoxCsvExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxCsvColumn && canUnquote(element)
    }

    private fun canUnquote(element: ParadoxCsvExpressionElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }
}
