package icu.windea.pls.core.search

import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.script.psi.*

class ParadoxSearchScopeType(
    val searchScopeType: String
) {
    fun findRoot(context: PsiElement): PsiElement? {
        return when(searchScopeType) {
            "definition" -> context.findParentDefinition()
            else -> null
        }
    }
    
    fun getGlobalSearchScope(context: PsiElement): GlobalSearchScope? {
        return when(searchScopeType) {
            "definition" -> GlobalSearchScope.fileScope(context.containingFile) //限定在当前文件作用域
            else -> null
        }
    }
}