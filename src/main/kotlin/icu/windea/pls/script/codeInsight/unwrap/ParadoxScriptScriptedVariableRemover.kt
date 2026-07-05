package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptScriptedVariableRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptScriptedVariable) e.name.or.unresolved() else ""
        return ChronicleBundle.message("script.remove.scriptedVariable", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptScriptedVariable
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
