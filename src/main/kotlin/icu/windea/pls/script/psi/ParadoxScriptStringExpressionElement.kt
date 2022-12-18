package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString 
 */
interface ParadoxScriptStringExpressionElement: ParadoxScriptExpressionElement, ContributedReferenceHost, NavigatablePsiElement {
	val stub: ParadoxScriptStringExpressionElementStub<*>?
	
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxScriptStringExpressionElement
}
