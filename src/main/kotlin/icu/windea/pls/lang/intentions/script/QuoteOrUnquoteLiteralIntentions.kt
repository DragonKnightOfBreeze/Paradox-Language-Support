@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.originalFileOrSelf
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.canQuote
import icu.windea.pls.core.canUnquote
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.quote
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.injection.ParadoxLanguageInjectionManager
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptNumber
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class QuoteLiteralIntention : PsiUpdateModCommandAction<ParadoxScriptExpressionElement>(ParadoxScriptExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.quoteLiteral")

    // NOTE 1.3.0+ 目前无法适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""` 中的 `\"v\"` ）

    override fun invoke(context: ActionContext, element: ParadoxScriptExpressionElement, updater: ModPsiUpdater) {
        val quotePattern = if (element is PsiQuoteAwareElement) element.quotePattern else QuotePatterns.Default
        val newText = element.text.quote(quotePattern)
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxScriptExpressionElement, context: ActionContext): Boolean {
        // can also be applied to number literals
        if (element is ParadoxScriptNumber) return true
        return element is ParadoxScriptStringExpressionElement && element.text.canQuote(element.quotePattern)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptExpressionElement
    }

    override fun isFileAllowed(file: PsiFile): Boolean {
        // 3.0.3 not allowed for injected context (compatible with `LightVirtualFileBase`)
        val fileToCheck = file.virtualFile.originalFileOrSelf().toPsiFile(file.project) ?: file
        if (ParadoxLanguageInjectionManager.isInjectedFileFromScriptFile(fileToCheck)) return false
        return true
    }
}

class UnquoteLiteralIntention : PsiUpdateModCommandAction<ParadoxScriptExpressionElement>(ParadoxScriptExpressionElement::class.java), DumbAware {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.unquoteLiteral")

    // NOTE 1.3.0+ 目前无法适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""` 中的 `\"v\"` ）

    override fun invoke(context: ActionContext, element: ParadoxScriptExpressionElement, updater: ModPsiUpdater) {
        val quotePattern = if (element is PsiQuoteAwareElement) element.quotePattern else QuotePatterns.Default
        val newText = element.text.unquote(quotePattern)
        ElementManipulators.handleContentChange(element, newText)
    }

    override fun isElementApplicable(element: ParadoxScriptExpressionElement, context: ActionContext): Boolean {
        return element is ParadoxScriptStringExpressionElement && element.text.canUnquote(element.quotePattern)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptExpressionElement
    }

    override fun isFileAllowed(file: PsiFile): Boolean {
        // 3.0.3 not allowed for injected context (compatible with `LightVirtualFileBase`)
        val fileToCheck = file.virtualFile.originalFileOrSelf().toPsiFile(file.project) ?: file
        if (ParadoxLanguageInjectionManager.isInjectedFileFromScriptFile(fileToCheck)) return false
        return true
    }
}
