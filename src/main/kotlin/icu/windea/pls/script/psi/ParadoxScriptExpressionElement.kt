package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptExpressionElement: ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost {
	val stub: ParadoxScriptExpressionElementStub<*>?
	
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxScriptExpressionElement
	
	fun getName(): String //注意这里不能使用默认方法，因为PsiElementBase存在默认的实现
	
	fun setName(name: String): ParadoxScriptExpressionElement //注意这里不能使用默认方法，因为PsiElementBase存在默认的实现
}
