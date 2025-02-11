package icu.windea.pls.cwt.psi

import com.intellij.psi.*
import icu.windea.pls.model.*

/**
 * @see CwtPropertyKey
 * @see CwtValue
 */
interface CwtExpressionElement : NavigatablePsiElement, CwtTypedElement {
    override fun getName(): String

    val value: String

    fun setValue(value: String): CwtExpressionElement

    override val type: CwtType
}
