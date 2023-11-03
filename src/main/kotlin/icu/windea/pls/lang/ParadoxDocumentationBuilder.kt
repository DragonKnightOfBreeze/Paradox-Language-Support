package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

object ParadoxDocumentationBuilder {
    fun getModifierCategoriesText(categories: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
        if(categories.isEmpty()) return ""
        return buildString {
            append("<code>")
            var appendSeparator = false
            for(category in categories) {
                if(appendSeparator) append(", ") else appendSeparator = true
                appendCwtLink("${gameType.linkToken}modifier_categories/$category", category, contextElement)
            }
            append("</code>")
        }
    }
    
    fun getScopeText(scopeId: String, gameType: ParadoxGameType, contextElement: PsiElement): String {
        return buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            append("</code>")
        }
    }
    
    fun getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
        if(scopeIds.isEmpty()) return ""
        return buildString {
            append("<code>")
            var appendSeparator = false
            for(scopeId in scopeIds) {
                if(appendSeparator) append(", ") else appendSeparator = true
                ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            }
            append("</code>")
        }
    }
    
    fun getScopeContextText(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement) : String{
        return buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeContextDoc(scopeContext, gameType, contextElement, this)
            append("</code>")
        }
    }
}