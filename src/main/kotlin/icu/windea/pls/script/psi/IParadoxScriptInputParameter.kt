package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 脚本输入参数。
 */
interface IParadoxScriptInputParameter : PsiNamedElement, ParadoxScriptExpression {
	override fun getName(): String
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
}