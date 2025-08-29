package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.isBlockMember

class ParadoxScriptBlockUnwrapper : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return PlsBundle.message("script.unwrap.block")
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptBlock && e.isBlockMember()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        if (element !is ParadoxScriptBlock) return
        context.extract(element, element)
        context.delete(element)
    }
}
