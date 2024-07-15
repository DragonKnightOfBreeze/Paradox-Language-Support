package icu.windea.pls.localisation.references

import com.intellij.patterns.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(ParadoxLocalisationExpressionElement::class.java), ParadoxLocalisationExpressionReferenceProvider())
    }
}


