package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

class CwtBlockRemover : CwtUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return PlsBundle.message("cwt.remove.block")
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is CwtBlock && e.isBlockValue()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
