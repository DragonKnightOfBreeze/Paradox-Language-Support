package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.model.*

/**
 * 脚本输入参数。
 */
interface IParadoxScriptInputParameter : PsiNamedElement, ParadoxScriptPsiExpression {
	override fun getName(): String
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
}