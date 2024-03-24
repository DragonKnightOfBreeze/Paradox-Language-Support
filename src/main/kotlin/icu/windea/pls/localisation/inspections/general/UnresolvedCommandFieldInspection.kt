package icu.windea.pls.localisation.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的命令字段的检查。
 */
class UnresolvedCommandFieldInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationCommandField) visitCommandField(element)
            }
            
            private fun visitCommandField(element: ParadoxLocalisationCommandField) {
                val location = element
                val reference = element.reference
                if(reference == null || reference.resolve() != null) return
                val name = element.name
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedCommandField.description", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }
}
