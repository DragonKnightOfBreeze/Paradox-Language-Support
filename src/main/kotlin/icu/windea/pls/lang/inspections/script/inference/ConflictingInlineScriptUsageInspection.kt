package icu.windea.pls.lang.inspections.script.inference

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*

/**
 * 检查内联脚本的使用位置是否存在冲突。
 */
class ConflictingInlineScriptUsageInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null
        val configContext = ParadoxExpressionManager.getConfigContext(file) ?: return null
        if (configContext.inlineScriptHasConflict != true) return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("script.annotator.inlineScript.conflict", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix())
        return holder.resultsArray
    }
}
