package icu.windea.pls.model

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.createPointer

data class ParadoxInlineMathArgument(
    val expression: String,
    val id: String,
    val defaultValue: String,
    var value: String = "",
) {
    private var resolvedElementPointer: SmartPsiElementPointer<PsiElement>? = null
    val resolvedElement: PsiElement? get() = resolvedElementPointer?.element

    fun withResolvedElement(element: PsiElement?, file: PsiFile? = element?.containingFile): ParadoxInlineMathArgument {
        resolvedElementPointer = element?.createPointer(file)
        return this
    }
}
