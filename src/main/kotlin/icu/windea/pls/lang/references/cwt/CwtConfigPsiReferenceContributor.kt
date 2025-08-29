package icu.windea.pls.lang.references.cwt

import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import icu.windea.pls.cwt.psi.CwtStringExpressionElement

class CwtConfigPsiReferenceContributor : PsiReferenceContributor() {
    private val symbolProvider = CwtConfigSymbolReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(CwtStringExpressionElement::class.java), symbolProvider)
    }
}
