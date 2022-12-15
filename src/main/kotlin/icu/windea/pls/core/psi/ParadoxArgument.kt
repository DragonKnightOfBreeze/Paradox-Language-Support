package icu.windea.pls.core.psi

import icu.windea.pls.core.expression.*

/**
 * 定义的传入参数。
 */
interface ParadoxArgument: ParadoxTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxArgument
	
	override val type: ParadoxDataType get() = ParadoxDataType.ParameterType
}
