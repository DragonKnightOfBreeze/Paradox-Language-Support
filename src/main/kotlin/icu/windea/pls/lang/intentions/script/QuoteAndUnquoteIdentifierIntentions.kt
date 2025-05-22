@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class QuoteIdentifierIntention : PsiUpdateModCommandAction<PsiElement>(PsiElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.quoteIdentifier")

    //NOTE 1.3.0+ 目前不直接适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""`中的的`\"v\"`）

    override fun invoke(context: ActionContext, element: PsiElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.quote())
    }

    override fun isElementApplicable(element: PsiElement, context: ActionContext): Boolean {
        //can also be applied to number value tokens
        return when (element) {
            is ParadoxScriptPropertyKey -> canQuote(element)
            is ParadoxScriptString -> canQuote(element)
            is ParadoxScriptInt -> true
            is ParadoxScriptFloat -> true
            else -> false
        }
    }

    private fun canQuote(element: PsiElement): Boolean {
        val text = element.text
        return !text.isQuoted()
    }
}

class UnquoteIdentifierIntention : PsiUpdateModCommandAction<PsiElement>(PsiElement::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.unquoteIdentifier")

    //NOTE 1.3.0+ 目前不直接适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""`中的的`\"v\"`）

    override fun invoke(context: ActionContext, element: PsiElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }

    override fun isElementApplicable(element: PsiElement, context: ActionContext): Boolean {
        return when (element) {
            is ParadoxScriptPropertyKey -> canUnquote(element)
            is ParadoxScriptString -> canUnquote(element)
            else -> false
        }
    }

    fun canUnquote(element: PsiElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }
}
