package icu.windea.pls.lang.references.cwt

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtConfigPsiReferenceContributor : PsiReferenceContributor() {
    private val symbolProvider = CwtConfigSymbolReferenceProvider()

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiElement(CwtStringExpressionElement::class.java), symbolProvider)
    }
}
