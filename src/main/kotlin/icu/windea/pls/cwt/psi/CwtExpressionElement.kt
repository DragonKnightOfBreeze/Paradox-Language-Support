package icu.windea.pls.cwt.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 可以作为表达式的 [PsiElement]。
 *
 * @see CwtPropertyKey
 * @see CwtValue
 */
interface CwtExpressionElement : NavigatablePsiElement, PsiPresentableTextAwareElement {
    override fun getName(): String

    val value: String get() = text

    fun setValue(value: String): CwtExpressionElement = throw IncorrectOperationException()

    fun setContent(content: String, range: TextRange): CwtExpressionElement = throw IncorrectOperationException()
}
