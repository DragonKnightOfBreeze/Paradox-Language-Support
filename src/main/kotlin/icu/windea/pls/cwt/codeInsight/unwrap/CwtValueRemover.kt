package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue

class CwtValueRemover : CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is CwtValue) e.name else ""
        return PlsBundle.message("cwt.remove.value", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return (e is CwtValue && e !is CwtBlock) && e.isBlockValue()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
