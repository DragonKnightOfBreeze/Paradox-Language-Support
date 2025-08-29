package icu.windea.pls.lang.references.csv

import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement

class ParadoxCsvPsiReferenceContributor : PsiReferenceContributor() {
    private val expressionProvider = ParadoxCsvExpressionPsiReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(ParadoxCsvExpressionElement::class.java), expressionProvider)
    }
}
