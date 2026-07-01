package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertyUnwrapper : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptProperty) e.name else ""
        return ChronicleBundle.message("script.unwrap.property", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptProperty && e.propertyValue is ParadoxScriptBlock
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptProperty) return
        val block = element.propertyValue
        if (block !is ParadoxScriptBlock) return
        context.extract(element, block)
        context.delete(element)
    }
}
