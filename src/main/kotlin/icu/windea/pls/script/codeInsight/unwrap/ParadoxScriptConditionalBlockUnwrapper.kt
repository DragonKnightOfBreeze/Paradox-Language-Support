package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock

class ParadoxScriptConditionalBlockUnwrapper : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptNormalConditionalBlock
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptNormalConditionalBlock) return "" // unexpected
        val text = element.presentableText
        return ChronicleBundle.message("script.unwrap.conditionalBlock", text)
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptNormalConditionalBlock) return // unexpected
        context.extract(element, element)
        context.delete(element)
    }
}
