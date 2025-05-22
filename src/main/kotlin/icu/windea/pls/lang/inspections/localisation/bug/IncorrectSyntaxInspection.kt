package icu.windea.pls.lang.inspections.localisation.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class IncorrectSyntaxInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkDanglingColorfulTextEndMarker(element)
                checkDanglingTextFormatEndMarker(element)
            }

            private fun checkDanglingColorfulTextEndMarker(element: PsiElement) {
                if (element.elementType != COLORFUL_TEXT_END) return
                if (element.nextSibling == null && element.parent?.elementType == COLORFUL_TEXT) return
                val message = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.1")
                val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1", element.text))
                holder.registerProblem(element, message, fix)
            }

            private fun checkDanglingTextFormatEndMarker(element: PsiElement) {
                if (element.elementType != TEXT_FORMAT_END) return
                if (element.nextSibling == null && element.parent?.elementType == TEXT_FORMAT) return
                val message = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.2")
                val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1", element.text))
                holder.registerProblem(element, message, fix)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }
}
