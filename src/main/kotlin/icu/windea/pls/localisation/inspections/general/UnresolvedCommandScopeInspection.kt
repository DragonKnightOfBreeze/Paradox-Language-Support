package icu.windea.pls.localisation.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的命令作用域的检查。
 */
class UnresolvedCommandScopeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationCommandScope) visitCommandScope(element)
            }
            
            private fun visitCommandScope(element: ParadoxLocalisationCommandScope) {
                val location = element
                val reference = element.reference
                if(reference.canResolve()) return
                val name = element.name
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.general.unresolvedCommandScope.description", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }
}
