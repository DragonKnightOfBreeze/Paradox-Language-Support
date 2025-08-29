package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.or
import icu.windea.pls.core.util.unresolved
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

class ParadoxScriptParameterConditionUnwrapper : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptParameterCondition) e.presentationText.or.unresolved() else ""
        return PlsBundle.message("script.unwrap.parameterCondition", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptParameterCondition
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.extract(element, element)
        context.delete(element)
    }
}
