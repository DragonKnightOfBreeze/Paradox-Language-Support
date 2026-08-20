package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查内联脚本是否未被使用。
 */
class UnusedInlineScriptInspection : InlineScriptInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                check(file, holder)
            }
        }
    }

    private fun check(file: PsiFile, holder: ProblemsHolder) {
        // still check if `inference.inlineScriptConfig` is not enabled
        // if (!getSettings().inference.inlineScriptConfig) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return
        val selector = ParadoxInlineScriptUsageSearch.selector(file.project, file)
        val hasUsages = ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).findFirst() != null
        if (hasUsages) return

        val description = ChronicleBundle.message("inspection.script.unusedInlineScript.desc", inlineScriptExpression)
        holder.registerProblem(file, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL)
    }
}
