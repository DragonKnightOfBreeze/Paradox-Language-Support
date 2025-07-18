package icu.windea.pls.lang.references.script

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPsiReferenceContributor : PsiReferenceContributor() {
    val provider = ParadoxScriptPsiReferenceProvider()
    val expressionReferenceProvider = ParadoxScriptExpressionPsiReferenceProvider()
    val enumNamespaceReferenceProvider = ParadoxEventNamespacePsiReferenceProvider()
    val typeKeyPrefixReferenceProvider = ParadoxScriptTypeKeyPrefixPsiReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(ParadoxScriptScriptedVariableReference::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxScriptInlineMathScriptedVariableReference::class.java), provider)

        registrar.registerReferenceProvider(psiElement(ParadoxScriptExpressionElement::class.java), expressionReferenceProvider)
        registrar.registerReferenceProvider(psiElement(ParadoxScriptString::class.java), enumNamespaceReferenceProvider)
        registrar.registerReferenceProvider(psiElement(ParadoxScriptString::class.java), typeKeyPrefixReferenceProvider)
    }
}
