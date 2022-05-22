package icu.windea.pls.script.psi

import icu.windea.pls.core.*
import icu.windea.pls.script.reference.*

interface IParadoxScriptVariableReference : ParadoxScriptExpression {
	val name: String
	
	fun setName(name: String): IParadoxScriptVariableReference
	
	override fun getReference(): ParadoxScriptVariableReferenceReference
	
	val referenceValue: ParadoxScriptValue? get() = reference.resolve()?.variableValue?.number
	
	override val valueType: ParadoxValueType? get() = referenceValue?.valueType
}