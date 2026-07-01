package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.fixes.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.resolve.inlineScriptHasConflict
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查内联脚本是否存在冲突的用法。
 */
class ConflictingInlineScriptUsageInspection : InlineScriptInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        // still check if `inference.inlineScriptConfig` is not enabled
        // if (!getSettings().inference.inlineScriptConfig) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return null
        if (configContext.inlineScriptHasConflict != true) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = ChronicleBundle.message("inspection.script.conflictingInlineScriptUsage.desc", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix(file))
        return holder.resultsArray
    }
}
