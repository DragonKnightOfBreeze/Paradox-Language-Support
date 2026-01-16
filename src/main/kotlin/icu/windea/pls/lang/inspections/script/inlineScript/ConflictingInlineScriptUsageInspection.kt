package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.resolve.inlineScriptHasConflict
import icu.windea.pls.lang.quickfix.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查内联脚本用法是否存在冲突。
 */
class ConflictingInlineScriptUsageInspection : InlineScriptInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return null
        if (configContext.inlineScriptHasConflict != true) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("script.annotator.inlineScript.conflict", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix())
        return holder.resultsArray
    }
}
