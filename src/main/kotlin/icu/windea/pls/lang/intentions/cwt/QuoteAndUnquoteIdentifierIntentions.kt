@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.cwt

import com.intellij.modcommand.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

class QuoteIdentifierIntention : PsiUpdateModCommandAction<CwtExpressionElement>(CwtExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.quoteIdentifier")

    override fun invoke(context: ActionContext, element: CwtExpressionElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.quote())
    }

    override fun isElementApplicable(element: CwtExpressionElement, context: ActionContext): Boolean {
        //can also be applied to number value tokens
        if (element is CwtValue && !(element.isPropertyValue() || element.isBlockValue())) return false
        return when (element) {
            is CwtPropertyKey -> canQuote(element)
            is CwtString -> canQuote(element)
            is CwtInt -> true
            is CwtFloat -> true
            else -> false
        }
    }

    private fun canQuote(element: CwtExpressionElement): Boolean {
        val text = element.text
        return !text.isQuoted()
    }
}

class UnquoteIdentifierIntention : PsiUpdateModCommandAction<CwtExpressionElement>(CwtExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.unquoteIdentifier")

    override fun invoke(context: ActionContext, element: CwtExpressionElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }

    override fun isElementApplicable(element: CwtExpressionElement, context: ActionContext): Boolean {
        if (element is CwtValue && !(element.isPropertyValue() || element.isBlockValue())) return false
        return when (element) {
            is CwtPropertyKey -> canUnquote(element)
            is CwtString -> canUnquote(element)
            else -> false
        }
    }

    private fun canUnquote(element: CwtExpressionElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }
}
