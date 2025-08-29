package icu.windea.pls.lang.search.scope.type

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

abstract class ParadoxSearchScopeType(
    val id: String,
    val text: String,
) {
    open fun findRoot(project: Project, context: Any?): PsiElement? {
        return null
    }

    open fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope? {
        return null
    }

    open fun distinctInFile(): Boolean {
        return true
    }
}
