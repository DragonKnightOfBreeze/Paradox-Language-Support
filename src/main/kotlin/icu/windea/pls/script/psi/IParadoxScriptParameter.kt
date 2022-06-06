package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 脚本参数。
 */
interface IParadoxScriptParameter : PsiNamedElement, PsiNameIdentifierOwner, ParadoxScriptExpression {
	override fun getName(): String
	
	val defaultValue: String? get() = null
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
}