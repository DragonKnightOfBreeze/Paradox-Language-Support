package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock

class ParadoxScriptInlineConditionalBlockRemover : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptInlineConditionalBlock
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptInlineConditionalBlock) return "" // unexpected
        val text = element.presentableText
        return ChronicleBundle.message("script.remove.inlineConditionalBlock", text)
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptInlineConditionalBlock) return // unexpected
        context.delete(element)
    }
}
