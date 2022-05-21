package icu.windea.pls.script.psi

import com.intellij.psi.PsiNamedElement
import icu.windea.pls.*
import icu.windea.pls.core.*
import javax.swing.*

/**
 * 脚本参数。
 */
interface IParadoxScriptParameter : PsiNamedElement, ParadoxScriptExpression {
	val defaultValue: String?
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.NumberType
}