package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.fixes.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查当前脚本文件是否被声明为内联脚本。
 *
 * NOTE 3.0.0 代码检查仅适用于于项目文件，因此默认禁用此代码检查，另外提供编辑器通知。
 */
class DeclaredInlineScriptInspection : InlineScriptInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        // still check if `inference.inlineScriptConfig` is not enabled
        // if (!getSettings().inference.inlineScriptConfig) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = ChronicleBundle.message("inspection.script.declaredInlineScript.desc", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix(file))
        return holder.resultsArray
    }
}
