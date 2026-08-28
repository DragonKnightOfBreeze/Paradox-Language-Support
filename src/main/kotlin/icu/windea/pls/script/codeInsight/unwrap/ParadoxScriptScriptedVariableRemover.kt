package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptScriptedVariableRemover : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptScriptedVariable
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptScriptedVariable) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("script.remove.scriptedVariable", name.or.unresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptScriptedVariable) return // unexpected
        context.delete(element)
    }
}
