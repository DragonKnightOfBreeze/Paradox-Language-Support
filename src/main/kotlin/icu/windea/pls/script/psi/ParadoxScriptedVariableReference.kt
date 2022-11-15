package icu.windea.pls.script.psi

import icu.windea.pls.script.exp.*
import icu.windea.pls.script.references.*

interface ParadoxScriptedVariableReference : ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): ParadoxScriptedVariableReference
	
	override fun getReference(): ParadoxScriptedVariableReferenceReference
	
	val referenceValue: ParadoxScriptValue? get() = reference.resolve()?.scriptedVariableValue?.value
	
	override val expressionType: ParadoxDataType? get() = referenceValue?.expressionType
}
