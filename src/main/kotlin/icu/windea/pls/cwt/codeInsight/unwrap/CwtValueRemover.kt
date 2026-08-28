package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isDirectValue

class CwtValueRemover : CwtUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is CwtValue && element.isDirectValue()
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is CwtValue) return "" // unexpected
        if (element is CwtBlock) return ChronicleBundle.message("cwt.remove.block")
        val text = element.presentableText
        return ChronicleBundle.message("cwt.remove.value", text)
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is CwtValue) return // unexpected
        context.delete(element)
    }
}
