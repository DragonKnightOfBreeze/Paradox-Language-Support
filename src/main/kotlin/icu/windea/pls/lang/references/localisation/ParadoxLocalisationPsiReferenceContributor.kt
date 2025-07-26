package icu.windea.pls.lang.references.localisation

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPsiReferenceContributor : PsiReferenceContributor() {
    private val provider = ParadoxLocalisationPsiReferenceProvider()
    private val expressionProvider = ParadoxLocalisationExpressionPsiReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationArgument::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationLocale::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationColorfulText::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationParameter::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationIcon::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationConceptCommand::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationTextFormat::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationTextIcon::class.java), provider)
        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationScriptedVariableReference::class.java), provider)

        registrar.registerReferenceProvider(psiElement(ParadoxLocalisationExpressionElement::class.java), expressionProvider)
    }
}
