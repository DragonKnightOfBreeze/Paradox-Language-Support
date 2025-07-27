@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.csv

import com.intellij.modcommand.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*

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
