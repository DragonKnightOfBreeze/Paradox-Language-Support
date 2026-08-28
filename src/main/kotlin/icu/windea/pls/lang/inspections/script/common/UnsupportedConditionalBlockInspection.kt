package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查是否在不支持的上下文中使用了参数化块。
 *
 * 规则如下：
 * - 不支持在内联脚本文件中使用参数化块。
 */
class UnsupportedConditionalBlockInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitNormalConditionalBlock(element: ParadoxScriptNormalConditionalBlock) {
                ProgressManager.checkCanceled()
                checkInlineScript(element, holder)
            }

            override fun visitInlineConditionalBlock(element: ParadoxScriptInlineConditionalBlock) {
                ProgressManager.checkCanceled()
                checkInlineScript(element, holder)
            }

            // TODO 3.0.2+ for `ParadoxScriptInlineMathConditionalBlock`
        }
    }

    private fun checkInlineScript(element: ParadoxScriptConditionalBlock, holder: ProblemsHolder) {
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        holder.registerProblem(element, ChronicleInspectionBundle.message("script.unsupportedConditionalBlock.desc.1"))
    }
}
