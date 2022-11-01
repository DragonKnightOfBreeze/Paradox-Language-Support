package icu.windea.pls.core.psi

import com.intellij.psi.*
import icu.windea.pls.script.psi.*

interface ParadoxExpressionAwareElement: ParadoxScriptTypedElement, PsiLiteralValue, ContributedReferenceHost, NavigatablePsiElement {
	val stub: ParadoxExpressionAwareElementStub<*>?
	
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxExpressionAwareElement
}
