package icu.windea.pls.script.psi

import com.intellij.psi.PsiNamedElement
import icu.windea.pls.*
import icu.windea.pls.core.*
import javax.swing.*

/**
 * 脚本参数。
 */
interface IParadoxScriptParameter : PsiNamedElement, ParadoxScriptExpression {
	override fun getName(): String
	
	val defaultValue: String? get() = null
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.Parameter
}