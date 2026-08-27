package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.psi.PsiPresentableElement

/**
 * @see CwtPropertyKey
 * @see CwtValue
 */
interface CwtExpressionElement : NavigatablePsiElement, PsiPresentableElement {
    override fun getName(): String

    val value: String get() = text

    fun setValue(value: String): CwtExpressionElement = throw IncorrectOperationException()

    fun setContent(content: String, range: TextRange): CwtExpressionElement = throw IncorrectOperationException()
}
