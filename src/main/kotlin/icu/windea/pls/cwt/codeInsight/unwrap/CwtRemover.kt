package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.*

abstract class CwtRemover(key: String): CwtUnwrapRemoveBase(key) {
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
