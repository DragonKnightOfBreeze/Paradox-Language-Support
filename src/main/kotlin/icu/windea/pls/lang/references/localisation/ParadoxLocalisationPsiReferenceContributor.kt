package icu.windea.pls.lang.references.localisation

import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import icu.windea.pls.localisation.psi.ParadoxLocalisationArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon

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
