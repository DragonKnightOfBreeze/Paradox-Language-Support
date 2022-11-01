package icu.windea.pls.script.expression.reference

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*

class ParadoxScriptReferenceContributor : PsiReferenceContributor() {
	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(psiElement(ParadoxExpressionAwareElement::class.java), ParadoxScriptExpressionElementReferenceProvider())
	}
}
