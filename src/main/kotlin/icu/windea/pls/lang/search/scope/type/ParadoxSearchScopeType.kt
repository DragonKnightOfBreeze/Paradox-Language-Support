package icu.windea.pls.lang.search.scope.type

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.lang.search.scope.ParadoxSearchScope

sealed class ParadoxSearchScopeType(
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

    class FromFiles(id: String, text: String, private val block: (project: Project, context: Any?) -> GlobalSearchScope?): ParadoxSearchScopeType(id, text) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope? {
            return block(project, context)
        }
    }

    class InFile(id: String, text: String, private val block: (project: Project, context: Any?) -> PsiElement?): ParadoxSearchScopeType(id, text) {
        override fun findRoot(project: Project, context: Any?): PsiElement? {
            return block(project, context)
        }

        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            return ParadoxSearchScope.fileScope(project, context)
        }

        override fun distinctInFile(): Boolean {
            return false
        }
    }
}
