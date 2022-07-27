package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptExpressionElement : ParadoxScriptNamedElement, ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost {
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxScriptExpressionElement
	
	override fun getName(): String = value
	
	override fun setName(name: String) = setValue(name)
}