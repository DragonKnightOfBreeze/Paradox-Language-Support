package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptExpressionElement: ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost {
	val stub: ParadoxScriptExpressionElementStub<*>?
	
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxScriptExpressionElement
}
