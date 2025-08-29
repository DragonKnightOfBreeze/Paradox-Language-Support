@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.cwt

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
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtFloat
import icu.windea.pls.cwt.psi.CwtInt
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import icu.windea.pls.cwt.psi.isPropertyValue

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
