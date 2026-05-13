package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.model.type.CwtTypeResolver

/**
 * @see CwtPropertyKey
 * @see CwtValue
 */
interface CwtExpressionElement : NavigatablePsiElement {
    override fun getName(): String

    val value: String

    fun setValue(value: String): CwtExpressionElement

    val expression: String get() = CwtTypeResolver.resolveExpression(this)

    val type: CwtExpressionType get() = CwtTypeResolver.resolveExpressionType(this)
}
