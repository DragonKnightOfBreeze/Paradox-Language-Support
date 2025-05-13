package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数条件块。
 */
class UnsupportedParameterConditionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkInlineScript(element, holder)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    private fun checkInlineScript(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxScriptParameterCondition) return
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterCondition.desc.1"))
    }
}
