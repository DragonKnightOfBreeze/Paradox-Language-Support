package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.resolve.ParadoxTriggerService
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

/**
 * （对于脚本文件）检查是否存在不正确的语法。
 *
 * 包括：
 * - 不期望的比较运算符。
 */
class IncorrectSyntaxInspection : LocalInspectionTool(), DumbAware {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkComparisonOperator(holder, element)
            }
        }
    }

    private fun checkComparisonOperator(holder: ProblemsHolder, element: PsiElement) {
        // 不期望的比较操作符（比较操作符的左值或者右值必须能表示一个数字）
        if (element !is ParadoxScriptProperty) return
        val token = element.findChild { it.elementType in ParadoxScriptTokenSets.COMPARISON_TOKENS } ?: return
        val numberRepresentable = ParadoxTriggerService.isNumberRepresentable(element)
        if (numberRepresentable == false) {
            val description = PlsBundle.message("inspection.script.incorrectSyntax.desc.1")
            holder.registerProblem(token, description, ProblemHighlightType.GENERIC_ERROR)
            return
        }
    }
}
