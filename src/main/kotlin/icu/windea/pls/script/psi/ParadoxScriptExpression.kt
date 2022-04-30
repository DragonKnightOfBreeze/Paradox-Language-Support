package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.*

/**
 * @see icu.windea.pls.core.ParadoxValueType
 * @property valueType 值的类型。基于PSI元素的类型。
 * @property type 最相关的类型。如定义的类型、定义元素的类型。基于CWT规则。可能与值的类型不匹配。
 */
interface ParadoxScriptExpression : PsiElement {
	val valueType: ParadoxValueType? get() = null
	val type: String? get() = null
}