@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.cwt

import com.intellij.modcommand.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

class QuoteIdentifierIntention : PsiUpdateModCommandAction<PsiElement>(PsiElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.cwt.quoteIdentifier")

    override fun invoke(context: ActionContext, element: PsiElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.quote())
    }

    override fun isElementApplicable(element: PsiElement, context: ActionContext): Boolean {
        //can also be applied to number value tokens
        return when (element) {
            is CwtOptionKey -> canQuote(element)
            is CwtPropertyKey -> canQuote(element)
            is CwtString -> canQuote(element)
            is CwtInt -> true
            is CwtFloat -> true
            else -> false
        }
    }

    private fun canQuote(element: PsiElement): Boolean {
        val text = element.text
        return !text.isQuoted()
    }
}

class UnquoteIdentifierIntention : PsiUpdateModCommandAction<PsiElement>(PsiElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.cwt.unquoteIdentifier")

    override fun invoke(context: ActionContext, element: PsiElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }

    override fun isElementApplicable(element: PsiElement, context: ActionContext): Boolean {
        return when (element) {
            is CwtOptionKey -> canUnquote(element)
            is CwtPropertyKey -> canUnquote(element)
            is CwtString -> canUnquote(element)
            else -> false
        }
    }

    private fun canUnquote(element: PsiElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }
}
