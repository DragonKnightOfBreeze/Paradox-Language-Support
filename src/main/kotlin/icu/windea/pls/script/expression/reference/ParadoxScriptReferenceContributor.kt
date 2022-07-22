package icu.windea.pls.script.expression.reference

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptReferenceContributor : PsiReferenceContributor() {
	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(psiElement(ParadoxScriptPropertyKey::class.java), ParadoxScriptKeyExpressionReferenceProvider())
		registrar.registerReferenceProvider(psiElement(ParadoxScriptString::class.java), ParadoxScriptValueExpressionReferenceProvider())
	}
}
