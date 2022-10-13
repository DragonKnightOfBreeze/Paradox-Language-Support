package icu.windea.pls.script.psi

import icu.windea.pls.core.model.*

/**
 * 定义的输入参数。
 */
interface ParadoxInputParameter: ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxInputParameter
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.ParameterType
}