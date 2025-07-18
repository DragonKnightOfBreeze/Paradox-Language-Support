package icu.windea.pls.lang.references.csv

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvPsiReferenceContributor : PsiReferenceContributor() {
    val expressionProvider = ParadoxCsvExpressionPsiReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(ParadoxCsvExpressionElement::class.java), expressionProvider)
    }
}
