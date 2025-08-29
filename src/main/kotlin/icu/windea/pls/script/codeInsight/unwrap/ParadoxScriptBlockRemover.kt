package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.isBlockMember

class ParadoxScriptBlockRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return PlsBundle.message("script.remove.block")
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptBlock && e.isBlockMember()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
