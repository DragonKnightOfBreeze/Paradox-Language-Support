package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty

class CwtPropertyUnwrapper : CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is CwtProperty) e.name else ""
        return PlsBundle.message("cwt.unwrap.property", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is CwtProperty && e.propertyValue is CwtBlock
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is CwtProperty) return
        val block = element.propertyValue
        if (block !is CwtBlock) return
        context.extract(element, block)
        context.delete(element)
    }
}
