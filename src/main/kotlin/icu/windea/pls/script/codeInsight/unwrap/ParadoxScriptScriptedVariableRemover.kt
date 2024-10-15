package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxScriptScriptedVariableRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptScriptedVariable) e.name.orUnresolved() else ""
        return PlsBundle.message("script.remove.scriptedVariable", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptScriptedVariable
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
