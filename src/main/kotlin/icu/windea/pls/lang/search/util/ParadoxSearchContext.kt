package icu.windea.pls.lang.search.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.model.ParadoxGameType

interface ParadoxSearchContext {
    val gameType: ParadoxGameType?
    val project: Project
    val scope: GlobalSearchScope

    fun isValid(): Boolean {
        if (!project.isInitialized || project.isDisposed) return false
        if (project.isDefault) return false
        if (SearchScope.isEmptyScope(scope)) return false
        return true
    }
}
