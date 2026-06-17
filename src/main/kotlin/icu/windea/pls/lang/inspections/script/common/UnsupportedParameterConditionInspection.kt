package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * （脚本文件中的）不支持的参数条件块的代码检查。
 *
 * 规则如下：
 * - 不支持在内联脚本文件中使用参数条件块。
 */
class UnsupportedParameterConditionInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitParameterCondition(element: ParadoxScriptParameterCondition) {
                ProgressManager.checkCanceled()
                checkInlineScript(holder, element)
            }
        }
    }

    private fun checkInlineScript(holder: ProblemsHolder, element: ParadoxScriptParameterCondition) {
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterCondition.desc.1"))
    }
}
