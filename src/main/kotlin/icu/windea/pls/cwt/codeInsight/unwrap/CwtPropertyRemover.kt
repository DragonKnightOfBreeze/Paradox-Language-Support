package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.psi.CwtProperty

class CwtPropertyRemover : CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is CwtProperty) e.name else ""
        return PlsBundle.message("cwt.remove.property", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is CwtProperty
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
