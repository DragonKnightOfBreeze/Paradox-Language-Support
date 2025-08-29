package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.or
import icu.windea.pls.core.util.unresolved
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptScriptedVariableRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptScriptedVariable) e.name.or.unresolved() else ""
        return PlsBundle.message("script.remove.scriptedVariable", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptScriptedVariable
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
