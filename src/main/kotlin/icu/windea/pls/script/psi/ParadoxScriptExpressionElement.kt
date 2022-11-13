package icu.windea.pls.script.psi

import icu.windea.pls.core.psi.*

interface ParadoxScriptExpressionElement: ParadoxScriptTypedElement, ParadoxExpressionElement {
	val stub: ParadoxScriptExpressionElementStub<*>?
	
	override fun getValue(): String
	
	override fun setValue(value: String): ParadoxScriptExpressionElement
}
