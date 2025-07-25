@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.model.*
import com.intellij.model.psi.*
import com.intellij.model.search.*
import com.intellij.openapi.project.*
import icu.windea.pls.cwt.psi.*

//org.intellij.plugins.markdown.model.psi.headers.HeaderAnchorSymbolReferenceProvider

class CwtConfigSymbolReferenceProvider : PsiSymbolReferenceProvider {
    override fun getReferences(element: PsiExternalReferenceHost, hints: PsiSymbolReferenceHints): Collection<PsiSymbolReference> {
        if (element !is CwtStringExpressionElement) return emptySet()
        return CwtConfigSymbolManager.getSymbolReferences(element)
    }

    override fun getSearchRequests(project: Project, target: Symbol): Collection<SearchRequest> {
        return emptySet()
    }
}
