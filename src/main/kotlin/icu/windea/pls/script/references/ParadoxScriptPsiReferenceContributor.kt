package icu.windea.pls.script.references

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPsiReferenceContributor : PsiReferenceContributor() {
    val expressionReferenceProvider = ParadoxScriptExpressionPsiReferenceProvider()
    val enumNamespaceReferenceProvider = ParadoxEventNamespacePsiReferenceProvider()
    val typeKeyPrefixReferenceProvider = ParadoxTypeKeyPrefixPsiReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(ParadoxScriptExpressionElement::class.java), expressionReferenceProvider)
        registrar.registerReferenceProvider(psiElement(ParadoxScriptString::class.java), enumNamespaceReferenceProvider)
        registrar.registerReferenceProvider(psiElement(ParadoxScriptString::class.java), typeKeyPrefixReferenceProvider)
    }
}
