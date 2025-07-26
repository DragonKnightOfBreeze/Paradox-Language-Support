@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.model.psi.*
import com.intellij.psi.*
import icu.windea.pls.config.util.CwtConfigSymbolManager
import icu.windea.pls.cwt.psi.*

class CwtConfigSymbolDeclarationProvider : PsiSymbolDeclarationProvider {
    override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<PsiSymbolDeclaration> {
        if (element !is CwtStringExpressionElement) return emptySet()
        return CwtConfigSymbolManager.getSymbolDeclarations(element, offsetInElement)
    }
}
