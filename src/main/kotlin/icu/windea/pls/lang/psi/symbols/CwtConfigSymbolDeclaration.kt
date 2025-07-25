@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.psi.CwtStringExpressionElement

class CwtConfigSymbolDeclaration(
    private val element: CwtStringExpressionElement,
    private val symbol: CwtConfigSymbol
) : PsiSymbolDeclaration {
    override fun getDeclaringElement(): PsiElement {
        return element
    }

    override fun getRangeInDeclaringElement(): TextRange {
        return symbol.rangeInElement
    }

    override fun getSymbol(): Symbol {
        return symbol
    }
}

