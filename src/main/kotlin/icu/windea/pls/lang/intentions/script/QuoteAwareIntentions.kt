@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

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
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString

sealed class QuoteAwareIntentionBase : PsiUpdateModCommandAction<ParadoxScriptExpressionElement>(ParadoxScriptExpressionElement::class.java), DumbAware {
    protected fun canQuote(element: ParadoxScriptExpressionElement): Boolean {
        val text = element.text
        return !text.isQuoted()
    }

    protected fun canUnquote(element: ParadoxScriptExpressionElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }
}

class QuoteIdentifierIntention : QuoteAwareIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.quoteIdentifier")

    // NOTE 1.3.0+ 目前无法适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""` 中的 `\"v\"` ）

    override fun invoke(context: ActionContext, element: ParadoxScriptExpressionElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.quote())
    }

    override fun isElementApplicable(element: ParadoxScriptExpressionElement, context: ActionContext): Boolean {
        // can also be applied to number value tokens
        return when (element) {
            is ParadoxScriptPropertyKey -> canQuote(element)
            is ParadoxScriptString -> canQuote(element)
            is ParadoxScriptInt -> true
            is ParadoxScriptFloat -> true
            else -> false
        }
    }
}

class UnquoteIdentifierIntention : QuoteAwareIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.unquoteIdentifier")

    // NOTE 1.3.0+ 目前无法适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""` 中的 `\"v\"` ）

    override fun invoke(context: ActionContext, element: ParadoxScriptExpressionElement, updater: ModPsiUpdater) {
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }

    override fun isElementApplicable(element: ParadoxScriptExpressionElement, context: ActionContext): Boolean {
        return when (element) {
            is ParadoxScriptPropertyKey -> canUnquote(element)
            is ParadoxScriptString -> canUnquote(element)
            else -> false
        }
    }
}
