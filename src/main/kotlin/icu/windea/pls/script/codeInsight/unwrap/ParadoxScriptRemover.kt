package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*

abstract class ParadoxScriptRemover(key: String): ParadoxScriptUnwrapRemoveBase(key) {
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
