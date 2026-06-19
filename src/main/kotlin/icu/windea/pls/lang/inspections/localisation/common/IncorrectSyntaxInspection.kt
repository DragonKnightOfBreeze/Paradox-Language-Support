package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.lang.fixes.ReplaceStringFix
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

/**
 * （对于本地化文件）检查是否存在不正确的语法。
 *
 * 检测于文法级别和语义级别。
 *
 * 包括：
 * - 不正确的对左方括号（[LEFT_BRACKET]）的转义。文法级别。
 * - 悬挂的彩色文本（[COLORFUL_TEXT]）的结束标记（[COLORFUL_TEXT_END]，`§!`）。文法级别。
 * - 悬挂的文本格式（[TEXT_FORMAT]）的结束标记（[TEXT_FORMAT_END]，`#!`）。文法级别。
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkIncorrectLeftBracketEscape(element, holder)
                checkDanglingColorfulTextEndMarker(element, holder)
                checkDanglingTextFormatEndMarker(element, holder)
            }
        }
    }

    private fun checkIncorrectLeftBracketEscape(element: PsiElement, holder: ProblemsHolder) {
        val indices = ParadoxSyntaxService.getIncorrectLeftBracketEscapeIndices(element, holder.file)
        if (indices.isEmpty()) return
        val description = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.1")
        val startOffset = element.startOffset
        for (index in indices) {
            val rangeInElement = TextRange.from(index, 2)
            val fix = ReplaceStringFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.1.name"), "[[", startOffset + index, 2)
            holder.registerProblem(element, rangeInElement, description, fix)
        }
    }

    private fun checkDanglingColorfulTextEndMarker(element: PsiElement, holder: ProblemsHolder) {
        if (!ParadoxSyntaxService.isDanglingColorfulTextEndMarker(element)) return
        val description = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.2")
        val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.2.name"))
        holder.registerProblem(element, description, fix)
    }

    private fun checkDanglingTextFormatEndMarker(element: PsiElement, holder: ProblemsHolder) {
        if (!ParadoxSyntaxService.isDanglingTextFormatEndMarker(element)) return
        val description = PlsBundle.message("inspection.localisation.incorrectSyntax.desc.3")
        val fix = DeleteStringByElementTypeFix(element, PlsBundle.message("inspection.localisation.incorrectSyntax.fix.2.name"))
        holder.registerProblem(element, description, fix)
    }
}
