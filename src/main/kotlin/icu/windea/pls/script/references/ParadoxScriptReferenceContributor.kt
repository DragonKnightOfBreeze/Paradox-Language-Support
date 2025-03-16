package icu.windea.pls.script.references

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptReferenceContributor : PsiReferenceContributor() {
    val expressionReferenceProvider = ParadoxScriptExpressionReferenceProvider()
    val enumNamespaceReferenceProvider = ParadoxEventNamespaceReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(ParadoxScriptExpressionElement::class.java), expressionReferenceProvider)
        registrar.registerReferenceProvider(psiElement(ParadoxScriptString::class.java), enumNamespaceReferenceProvider)
    }
}
