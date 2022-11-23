package icu.windea.pls.core.psi

import icu.windea.pls.script.exp.*
import icu.windea.pls.script.psi.*

/**
 * 定义的传入参数。
 */
interface ParadoxArgument: ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxArgument
	
	override val expressionType: ParadoxDataType get() = ParadoxDataType.ParameterType
}
