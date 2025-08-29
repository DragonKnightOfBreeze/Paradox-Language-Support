package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.isBlockValue

class CwtBlockUnwrapper : CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return PlsBundle.message("cwt.unwrap.block")
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is CwtBlock && e.isBlockValue()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is CwtBlock) return
        context.extract(element, element)
        context.delete(element)
    }
}
