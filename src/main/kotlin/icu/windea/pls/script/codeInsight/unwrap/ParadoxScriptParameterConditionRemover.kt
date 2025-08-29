package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.or
import icu.windea.pls.core.util.unresolved
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

class ParadoxScriptParameterConditionRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptParameterCondition) e.presentationText.or.unresolved() else ""
        return PlsBundle.message("script.remove.parameterCondition", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptParameterCondition
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
