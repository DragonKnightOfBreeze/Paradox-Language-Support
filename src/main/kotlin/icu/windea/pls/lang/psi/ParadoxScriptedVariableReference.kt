package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 封装变量引用的共用接口。
 */
interface ParadoxScriptedVariableReference : NavigatablePsiElement, PsiPresentableTextAwareElement {
    val idElement: PsiElement?

    fun setName(name: String): ParadoxScriptedVariableReference
}
