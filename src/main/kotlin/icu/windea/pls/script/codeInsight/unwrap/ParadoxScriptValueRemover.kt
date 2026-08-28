package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDirectValue

class ParadoxScriptValueRemover : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptValue && element.isDirectValue()
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptValue) return "" // unexpected
        if (element is ParadoxScriptBlock) return ChronicleBundle.message("script.remove.block")
        val text = element.presentableText
        return ChronicleBundle.message("script.remove.value", text)
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptValue) return // unexpected
        context.delete(element)
    }
}
