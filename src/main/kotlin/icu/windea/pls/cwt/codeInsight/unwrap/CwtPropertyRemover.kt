package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.cwt.psi.CwtProperty

class CwtPropertyRemover : CwtUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is CwtProperty
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is CwtProperty) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("cwt.remove.property", name.orUnresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is CwtProperty) return // unexpected
        context.delete(element)
    }
}
