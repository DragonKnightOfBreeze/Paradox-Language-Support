package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.expression.*

/**
 * @property definitionType 定义的类型。基于CWT规则。
 * @property expressionType 脚本表达式的类型。基于PSI元素的类型。
 * @property expression 脚本表达式。如：`key = value` `key = {...}` `value`，保留括起的双引号
 * @property configExpression 定义元素对应的规则表达式。基于CWT规则。
 */
interface ParadoxScriptTypedElement : PsiElement {
	val definitionType: String? get() = null
	val expressionType: ParadoxExpressionType? get() = null
	val expression: String get() = text
	val configExpression: String? get() = null
}