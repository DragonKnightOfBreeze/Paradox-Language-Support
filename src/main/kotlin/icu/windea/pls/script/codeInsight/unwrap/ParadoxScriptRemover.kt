package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*

abstract class ParadoxScriptRemover(key: String): ParadoxScriptUnwrapRemoveBase(key) {
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
