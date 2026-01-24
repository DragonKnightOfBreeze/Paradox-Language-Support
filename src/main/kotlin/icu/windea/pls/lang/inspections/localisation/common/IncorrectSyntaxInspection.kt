package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.indicesOf
import icu.windea.pls.lang.quickfix.DeleteStringByElementTypeFix
import icu.windea.pls.lang.quickfix.ReplaceStringFix
import icu.windea.pls.lang.util.PlsFileManager
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
                ProgressManager.checkCanceled()
                checkIncorrectLeftBracketEscape(holder, element)
                checkDanglingColorfulTextEndMarker(holder, element)
                checkDanglingTextFormatEndMarker(holder, element)
            }
        }
    }

    private fun checkIncorrectLeftBracketEscape(holder: ProblemsHolder, element: PsiElement) {
        if (element.elementType != TEXT_TOKEN) return
        if (PlsFileManager.isInjectedFile(holder.file.virtualFile)) return // only for actual localisation files, skip injected files (e.g., in script strings)
        val text = element.text
        val indices = text.indicesOf("\\[")
        if (indices.isEmpty()) return
        val description = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.1")
        val startOffset = element.startOffset
        for (index in indices) {
            val rangeInELement = TextRange.from(index, 2)
            val fix = ReplaceStringFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1.name"), "[[", startOffset + index, 2)
            holder.registerProblem(element, rangeInELement, description, fix)
        }
    }

    private fun checkDanglingColorfulTextEndMarker(holder: ProblemsHolder, element: PsiElement) {
        if (element.elementType != COLORFUL_TEXT_END) return
        if (element.nextSibling == null && element.parent?.elementType == COLORFUL_TEXT) return
        val description = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.2")
        val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.2.name", element.text))
        holder.registerProblem(element, description, fix)
    }

    private fun checkDanglingTextFormatEndMarker(holder: ProblemsHolder, element: PsiElement) {
        if (element.elementType != TEXT_FORMAT_END) return
        if (element.nextSibling == null && element.parent?.elementType == TEXT_FORMAT) return
        val description = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.3")
        val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.2.name", element.text))
        holder.registerProblem(element, description, fix)
    }
}
