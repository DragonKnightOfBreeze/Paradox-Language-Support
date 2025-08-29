package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.or
import icu.windea.pls.core.util.unresolved
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition

class ParadoxScriptInlineParameterConditionRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptInlineParameterCondition) e.presentationText.or.unresolved() else ""
        return PlsBundle.message("script.remove.inlineParameterCondition", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptInlineParameterCondition
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
