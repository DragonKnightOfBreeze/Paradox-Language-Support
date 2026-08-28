package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty

class CwtPropertyUnwrapper : CwtUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is CwtProperty && element.propertyValue is CwtBlock
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is CwtProperty) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("cwt.unwrap.property", name.or.unresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is CwtProperty) return // unexpected
        val block = element.propertyValue
        if (block !is CwtBlock) return
        context.extract(element, block)
        context.delete(element)
    }
}
