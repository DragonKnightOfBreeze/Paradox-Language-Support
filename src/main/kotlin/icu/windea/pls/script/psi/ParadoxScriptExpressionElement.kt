package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptExpressionElement: ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost, NavigatablePsiElement {
	val stub: ParadoxScriptExpressionElementStub<*>?
	
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxScriptExpressionElement
}
