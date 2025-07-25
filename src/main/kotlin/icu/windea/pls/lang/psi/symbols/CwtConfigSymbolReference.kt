@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.model.*
import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

//org.intellij.plugins.markdown.model.psi.headers.HeaderAnchorLinkDestinationReference

class CwtConfigSymbolReference(
    private val element: CwtStringExpressionElement,
    private val rangeInElement: TextRange,
    val name: String,
    val configType: CwtConfigType,
    val gameType: ParadoxGameType
) : PsiSymbolReference {
    override fun getElement(): PsiElement {
        return element
    }

    override fun getRangeInElement(): TextRange {
        return rangeInElement
    }

    override fun resolveReference(): Collection<Symbol> {
        return CwtConfigSymbolManager.resolveSymbolReference(this)
    }
}
