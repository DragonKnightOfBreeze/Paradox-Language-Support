package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch
import icu.windea.pls.lang.search.selector.inlineScriptUsage
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 未使用的内联脚本的检查。
 */
class UnusedInlineScriptInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        //still check if inference.inlineScriptConfig is not checked
        //if(!getSettings().inference.inlineScriptConfig) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null
        val selector = selector(file.project, file).inlineScriptUsage()
        val hasUsages = ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).findFirst() != null
        if (hasUsages) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("inspection.script.unusedInlineScript.desc", inlineScriptExpression)
        holder.registerProblem(file, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL)
        return holder.resultsArray
    }
}
