package icu.windea.pls.core.psi

import com.intellij.psi.*
import icu.windea.pls.model.*

/**
 * @property type 脚本表达式的类型。基于PSI元素的类型。
 * @property expression 脚本表达式。如：`key = value` `key = {...}` `value`，保留括起的双引号
 * @property configExpression 对应的规则表达式。基于CWT规则。
 */
interface ParadoxTypedElement : PsiElement {
	val type: ParadoxType? get() = null
	val expression: String? get() = null
	val configExpression: String? get() = null
}

