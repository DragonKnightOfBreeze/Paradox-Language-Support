package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isBlockValue

class ParadoxScriptBlockUnwrapper : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return ChronicleBundle.message("script.unwrap.block")
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptBlock && e.isBlockValue()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptBlock) return
        context.extract(element, element)
        context.delete(element)
    }
}
