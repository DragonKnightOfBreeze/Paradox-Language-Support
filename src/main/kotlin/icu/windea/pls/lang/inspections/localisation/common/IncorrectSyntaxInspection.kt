package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.DeleteStringByElementTypeFix
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

/**
 * （对于本地化文件）检查是否存在不正确的语法。
 *
 * - 报告悬挂的彩色文本（[COLORFUL_TEXT]）的结束标记（[COLORFUL_TEXT_END]，`§!`）。
 * - 报告悬挂的文本格式（[TEXT_FORMAT]）的结束标记（[TEXT_FORMAT_END]，`#!`）。
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                checkDanglingColorfulTextEndMarker(element)
                checkDanglingTextFormatEndMarker(element)
            }

            private fun checkDanglingColorfulTextEndMarker(element: PsiElement) {
                if (element.elementType != COLORFUL_TEXT_END) return
                if (element.nextSibling == null && element.parent?.elementType == COLORFUL_TEXT) return
                val message = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.1")
                val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1.name", element.text))
                holder.registerProblem(element, message, fix)
            }

            private fun checkDanglingTextFormatEndMarker(element: PsiElement) {
                if (element.elementType != TEXT_FORMAT_END) return
                if (element.nextSibling == null && element.parent?.elementType == TEXT_FORMAT) return
                val message = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.2")
                val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1.name", element.text))
                holder.registerProblem(element, message, fix)
            }
        }
    }
}
