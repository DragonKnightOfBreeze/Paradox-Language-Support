package icu.windea.pls.script.psi

import icu.windea.pls.core.model.*

/**
 * 脚本输入参数。
 */
interface IParadoxScriptInputParameter : ParadoxScriptNamedElement, ParadoxScriptTypedElement {
	override fun getName(): String
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
}