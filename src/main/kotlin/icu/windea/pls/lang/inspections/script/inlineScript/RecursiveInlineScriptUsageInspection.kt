package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.resolve.inlineScriptHasRecursion
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查内联脚本是否存在递归的用法。
 */
class RecursiveInlineScriptUsageInspection : InlineScriptInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        // still check if `inference.inlineScriptConfig` is not enabled
        // if (!getSettings().inference.inlineScriptConfig) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return null
        if (configContext.inlineScriptHasRecursion != true) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("inspection.script.recursiveInlineScriptUsage.desc", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix(file))
        return holder.resultsArray
    }
}
