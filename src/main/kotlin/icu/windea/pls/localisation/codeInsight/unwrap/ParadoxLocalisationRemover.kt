package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.*

abstract class ParadoxLocalisationRemover(key: String): ParadoxLocalisationUnwrapRemoveBase(key) {
    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
