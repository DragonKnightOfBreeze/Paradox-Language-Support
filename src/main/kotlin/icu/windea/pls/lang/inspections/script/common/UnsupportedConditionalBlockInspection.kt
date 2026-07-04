package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * （脚本文件中的）不支持的参数化快的代码检查。
 *
 * 规则如下：
 * - 不支持在内联脚本文件中使用参数化快。
 */
class UnsupportedConditionalBlockInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitConditionalBlock(element: ParadoxScriptConditionalBlock) {
                ProgressManager.checkCanceled()
                checkInlineScript(element, holder)
            }
        }
    }

    private fun checkInlineScript(element: ParadoxScriptConditionalBlock, holder: ProblemsHolder) {
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        holder.registerProblem(element, ChronicleBundle.message("inspection.script.unsupportedConditionalBlock.desc.1"))
    }
}
