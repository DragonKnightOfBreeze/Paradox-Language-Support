package icu.windea.pls.lang.inspections.script.inference

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.configContext.inlineScriptHasRecursion
import icu.windea.pls.lang.quickfix.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查内联脚本的使用位置是否存在递归。
 */
class RecursiveInlineScriptUsageInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null
        val configContext = ParadoxExpressionManager.getConfigContext(file) ?: return null
        if (configContext.inlineScriptHasRecursion != true) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("script.annotator.inlineScript.recursive", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix())
        return holder.resultsArray
    }
}
