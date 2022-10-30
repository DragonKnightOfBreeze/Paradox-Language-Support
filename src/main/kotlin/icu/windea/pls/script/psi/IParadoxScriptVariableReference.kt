package icu.windea.pls.script.psi

import icu.windea.pls.script.expression.*
import icu.windea.pls.script.reference.*

interface IParadoxScriptVariableReference : ParadoxScriptTypedElement {
	val name: String
	
	fun setName(name: String): IParadoxScriptVariableReference
	
	override fun getReference(): ParadoxScriptedVariableReference
	
	val referenceValue: ParadoxScriptValue? get() = reference.resolve()?.variableValue?.value
	
	override val expressionType: ParadoxScriptExpressionType? get() = referenceValue?.expressionType
}