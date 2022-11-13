package icu.windea.pls.script.psi

import icu.windea.pls.core.expression.*

/**
 * 定义的参数。
 */
interface ParadoxParameter : ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxParameter
	
	val defaultValue: String? get() = null
	
	override val expressionType: ParadoxDataType get() = ParadoxDataType.ParameterType
}