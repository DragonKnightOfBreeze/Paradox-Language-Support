package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数条件块。
 */
class UnsupportedParameterConditionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                checkInlineScript(element, holder)
            }
        }
    }

    private fun checkInlineScript(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxScriptParameterCondition) return
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterCondition.desc.1"))
    }
}
