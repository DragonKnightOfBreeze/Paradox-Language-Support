package icu.windea.pls.lang.inspections.script.inlineScript

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.lang.fixes.GotoInlineScriptUsagesFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.resolve.inlineScriptHasRecursion
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

/**
 * 检查内联脚本是否存在递归的用法。
 */
class RecursiveInlineScriptUsageInspection : InlineScriptInspectionBase() {
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
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return
        if (configContext.inlineScriptHasRecursion != true) return

        val description = ChronicleInspectionBundle.message("script.recursiveInlineScriptUsage.desc", inlineScriptExpression)
        holder.registerProblem(file, description, GotoInlineScriptUsagesFix(file))
    }
}
