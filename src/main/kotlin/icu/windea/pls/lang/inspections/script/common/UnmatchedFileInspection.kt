package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fixes.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.inspections.script.inlineScript.InlineScriptInspectionBase
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig

/**
 * 检查当前脚本文件是否未匹配任何规则（包括：类型规则、复杂枚举规则）。
 *
 * 说明：
 * - 跳过直接位于游戏或入口目录下的文件。
 * - 跳过注入的文件或临时文件。
 *
 * @see CwtFilePathMatchableConfig
 * @see CwtTypeConfig
 * @see CwtComplexEnumConfig
 */
class UnmatchedFileInspection : InlineScriptInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        // still check if `inference.inlineScriptConfig` is not enabled
        // if (!getSettings().inference.inlineScriptConfig) return null

        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return null

        val holder = ProblemsHolder(manager, file, isOnTheFly)
        val description = PlsBundle.message("inspection.script.declaredInlineScript.desc", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix(file))
        return holder.resultsArray
    }
}
