package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

class ParadoxScriptValueRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptValue) e.name else ""
        return PlsBundle.message("script.remove.value", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return (e is ParadoxScriptValue && e !is ParadoxScriptBlock) && e.isBlockMember()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
