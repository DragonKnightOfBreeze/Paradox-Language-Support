@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import icu.windea.pls.core.quote
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class QuoteLiteralIntention : PsiUpdateModCommandAction<ParadoxScriptExpressionElement>(ParadoxScriptExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.quoteLiteral")

    // NOTE 1.3.0+ 目前无法适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""` 中的 `\"v\"` ）

    override fun invoke(context: ActionContext, element: ParadoxScriptExpressionElement, updater: ModPsiUpdater) {
        val newText = element.text.quote(lenient = true)
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxScriptExpressionElement, context: ActionContext): Boolean {
        // can also be applied to number literals
        if (element is ParadoxScriptNumberExpressionElement) return true
        return element is ParadoxScriptStringExpressionElement && element.canQuote(element.text)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptExpressionElement
    }
}

class UnquoteLiteralIntention : PsiUpdateModCommandAction<ParadoxScriptExpressionElement>(ParadoxScriptExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.unquoteLiteral")

    // NOTE 1.3.0+ 目前无法适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""` 中的 `\"v\"` ）

    override fun invoke(context: ActionContext, element: ParadoxScriptExpressionElement, updater: ModPsiUpdater) {
        val newText = element.text.unquote()
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxScriptExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxScriptStringExpressionElement && element.canUnquote(element.text)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptExpressionElement
    }
}
