package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

class ParadoxScriptValueRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return when (e) {
            is ParadoxScriptBlock -> PlsBundle.message("script.remove.block")
            is ParadoxScriptValue -> PlsBundle.message("script.remove.value", e.name)
            else -> throw IllegalStateException()
        }
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptValue && e.isBlockMember()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
