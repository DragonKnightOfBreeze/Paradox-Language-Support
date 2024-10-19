package icu.windea.pls.lang.psi

import com.intellij.psi.*
import icu.windea.pls.model.*

/**
 * @property type 类型。基于PSI的类型。
 * @property expression 表达式。保留括起的双引号。
 * @property configExpression 对应的规则表达式。基于CWT规则。
 */
interface ParadoxTypedElement : PsiElement {
    val type: ParadoxType? get() = null
    val expression: String? get() = null
    val configExpression: String? get() = null
}
