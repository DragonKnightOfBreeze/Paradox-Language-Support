package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertyUnwrapper : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty && element.propertyValue is ParadoxScriptBlock
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptProperty) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("script.unwrap.property", name.or.unresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptProperty) return // unexpected
        val block = element.propertyValue
        if (block !is ParadoxScriptBlock) return
        context.extract(element, block)
        context.delete(element)
    }
}
