package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.search.scope.ParadoxSearchScope

/**
 * @see ReadWriteAccess
 */
data class ParadoxAccessInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
) {
    // it's unnecessary to make it synced
    val statusMap = mutableMapOf<PsiElement, Boolean>()
    // compute once per file
    val searchScope by lazy { ParadoxSearchScope.fromFile(holder.project, holder.file.virtualFile) }
}
