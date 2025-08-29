package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertyRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptProperty) e.name else ""
        return PlsBundle.message("script.remove.property", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptProperty
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
