package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * 脚本表达式。复杂的脚本表达式会拥有额外的表达式信息。
 */
interface ParadoxScriptExpression : ParadoxScriptNamedElement, ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost {
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxScriptExpression
	
	override fun getName(): String = value
	
	override fun setName(name: String) = setValue(name)
}