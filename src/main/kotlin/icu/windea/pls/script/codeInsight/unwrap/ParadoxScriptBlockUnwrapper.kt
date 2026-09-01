package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.isDirectValue

class ParadoxScriptBlockUnwrapper : ParadoxScriptUnwrapper() {
    override fun isApplicableTo(element: PsiElement): Boolean {
        return element is ParadoxScriptBlock && element.isDirectValue() && element.leftBound != null && element.rightBound != null
    }

    override fun getDescription(element: PsiElement): String {
        if (element !is ParadoxScriptBlock) return "" // unexpected
        return ChronicleBundle.message("script.unwrap.block")
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptBlock) return // unexpected
        context.extract(element, element)
        context.delete(element)
    }
}
