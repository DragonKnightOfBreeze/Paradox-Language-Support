package icu.windea.pls.script.psi

import icu.windea.pls.model.*
import icu.windea.pls.script.reference.*

interface IParadoxScriptVariableReference : ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): IParadoxScriptVariableReference
	
	override fun getReference(): ParadoxScriptedVariableReference
	
	val referenceValue: ParadoxScriptValue? get() = reference.resolve()?.variableValue?.value
	
	override val valueType: ParadoxValueType? get() = referenceValue?.valueType
}