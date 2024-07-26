package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * 无法解析的颜色的检查。
 */
class UnresolvedColorInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationColorfulText) visitColorfulText(element)
            }
            
            private fun visitColorfulText(element: ParadoxLocalisationColorfulText) {
                val location = element.idElement ?: return
                val reference = element.reference
                if(reference == null || reference.resolve() != null) return
                val name = element.name ?: return
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedColor.desc", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }
}
