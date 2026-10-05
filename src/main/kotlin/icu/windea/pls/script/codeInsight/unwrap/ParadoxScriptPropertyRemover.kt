package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptPropertyRemover : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptProperty) return "" // unexpected
        val name = element.name
        return ChronicleBundle.message("script.remove.property", name.orUnresolved())
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptProperty) return // unexpected
        context.delete(element)
    }
}
