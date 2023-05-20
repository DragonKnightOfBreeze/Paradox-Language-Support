package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*

object ParadoxDocumentBuilder {
    fun getModifierCategoriesText(categories: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            var appendSeparator = false
            append("<code>")
            for(category in categories) {
                if(appendSeparator) append(", ") else appendSeparator = true
                appendCwtLink("${gameType.linkToken}modifier_categories/$category", category, contextElement)
            }
            append("</code>")
        }
    }
    
    fun getScopeText(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            append("</code>")
        }
    }
    
    fun getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            var appendSeparator = false
            append("<code>")
            for(scopeId in scopeIds) {
                if(appendSeparator) append(", ") else appendSeparator = true
                ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            }
            append("</code>")
        }
    }
}