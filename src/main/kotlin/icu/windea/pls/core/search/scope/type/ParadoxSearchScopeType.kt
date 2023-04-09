package icu.windea.pls.core.search.scope.type

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*

abstract class ParadoxSearchScopeType(
    val id: String,
    val text: String,
) { 
    open fun findRoot(project: Project, context: PsiElement): PsiElement? {
        return null
    }
    
    open fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope? {
        return null
    }
}