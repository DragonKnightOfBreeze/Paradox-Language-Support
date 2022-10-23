package icu.windea.pls.script.psi

import icu.windea.pls.core.model.*

/**
 * 定义的传入参数。
 */
interface ParadoxArgument: ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxArgument
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
}