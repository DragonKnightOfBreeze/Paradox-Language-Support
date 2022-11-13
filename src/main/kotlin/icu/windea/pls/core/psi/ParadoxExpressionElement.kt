package icu.windea.pls.core.psi

import com.intellij.psi.*

interface ParadoxExpressionElement: PsiLiteralValue, ContributedReferenceHost, NavigatablePsiElement {
	override fun getValue(): String
	
	fun setValue(value: String): ParadoxExpressionElement
}
