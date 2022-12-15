package icu.windea.pls.core.psi

import icu.windea.pls.core.expression.*

/**
 * 定义的参数。
 */
interface ParadoxParameter : ParadoxTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxParameter
	
	val defaultValue: String? get() = null
	
	override val type: ParadoxDataType get() = ParadoxDataType.ParameterType
}
