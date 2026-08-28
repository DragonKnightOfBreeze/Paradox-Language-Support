package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.isDirectValue

class CwtBlockUnwrapper : CwtUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is CwtBlock && element.isDirectValue()
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is CwtBlock) return "" // unexpected
        return ChronicleBundle.message("cwt.unwrap.block")
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is CwtBlock) return // unexpected
        context.extract(element, element)
        context.delete(element)
    }
}
