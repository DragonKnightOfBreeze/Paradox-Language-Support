package icu.windea.pls.core.psi

import icu.windea.pls.core.references.*
import icu.windea.pls.script.exp.*
import icu.windea.pls.script.psi.*

interface ParadoxScriptedVariableReference : ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxScriptedVariableReference
	
	override fun getReference(): ParadoxScriptedVariablePsiReference
	
	val referenceValue: ParadoxScriptValue? get() = reference.resolve()?.scriptedVariableValue
	
	override val expressionType: ParadoxDataType? get() = referenceValue?.expressionType
}
