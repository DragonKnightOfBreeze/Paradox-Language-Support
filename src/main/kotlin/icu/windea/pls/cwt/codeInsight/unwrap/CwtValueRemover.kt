package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue

class CwtValueRemover : CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return when (e) {
            is CwtBlock -> PlsBundle.message("cwt.remove.block")
            is CwtValue -> PlsBundle.message("cwt.remove.value", e.name)
            else -> throw IllegalStateException()
        }
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is CwtValue && e.isBlockValue()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
