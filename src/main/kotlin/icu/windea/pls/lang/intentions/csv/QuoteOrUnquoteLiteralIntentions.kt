@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.csv

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import icu.windea.pls.core.canQuote
import icu.windea.pls.core.canUnquote
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.quote
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle

class QuoteLiteralIntention : PsiUpdateModCommandAction<ParadoxCsvExpressionElement>(ParadoxCsvExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.quoteLiteral")

    override fun invoke(context: ActionContext, element: ParadoxCsvExpressionElement, updater: ModPsiUpdater) {
        val quotePattern = if (element is PsiQuoteAwareElement) element.quotePattern else QuotePatterns.Default
        val newText = element.text.quote(quotePattern, lenient = true)
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxCsvExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxCsvColumn && element.text.canQuote(element.quotePattern)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxCsvExpressionElement
    }
}

class UnquoteLiteralIntention : PsiUpdateModCommandAction<ParadoxCsvExpressionElement>(ParadoxCsvExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.unquoteLiteral")

    override fun invoke(context: ActionContext, element: ParadoxCsvExpressionElement, updater: ModPsiUpdater) {
        val quotePattern = if (element is PsiQuoteAwareElement) element.quotePattern else QuotePatterns.Default
        val newText = element.text.unquote(quotePattern)
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxCsvExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxCsvColumn && element.text.canUnquote(element.quotePattern)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxCsvExpressionElement
    }
}
