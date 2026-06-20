package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock

class ParadoxScriptInlineConditionalBlockRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptInlineConditionalBlock) e.presentationText.or.unresolved() else ""
        return PlsBundle.message("script.remove.inlineConditionalBlock", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptInlineConditionalBlock
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
