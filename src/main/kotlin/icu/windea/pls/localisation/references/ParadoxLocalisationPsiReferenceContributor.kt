package icu.windea.pls.localisation.references

import com.intellij.patterns.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPsiReferenceContributor : PsiReferenceContributor() {
    val expressionProvider = ParadoxLocalisationExpressionPsiReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(ParadoxLocalisationExpressionElement::class.java), expressionProvider)
    }
}


