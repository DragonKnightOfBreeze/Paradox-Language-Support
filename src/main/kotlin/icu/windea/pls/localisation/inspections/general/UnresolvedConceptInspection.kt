package icu.windea.pls.localisation.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的概念的检查。
 */
class UnresolvedConceptInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationConcept) visitConcept(element)
            }
            
            private fun visitConcept(element: ParadoxLocalisationConcept) {
                val location = element.conceptName
                val reference = element.reference
                if(reference == null || reference.resolve() != null) return
                val name = element.name
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedConcept.description", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }
}
