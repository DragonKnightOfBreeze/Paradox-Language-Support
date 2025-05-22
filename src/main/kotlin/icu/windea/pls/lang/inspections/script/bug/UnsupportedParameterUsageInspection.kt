package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数。
 */
class UnsupportedParameterUsageInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                checkGeneral(element, holder)
                checkInlineScript(element, holder)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    private fun checkGeneral(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxParameter && element !is ParadoxConditionParameter) return
        if (element.reference?.resolve() != null) return
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterUsage.desc.1"))
    }

    private fun checkInlineScript(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxScriptParameter) return
        if (element.defaultValue == null) return
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        val fix = getDeleteDefaultValueFix(element)
        holder.registerProblem(element, PlsBundle.message("inspection.script.unsupportedParameterUsage.desc.2"), fix)
    }

    private fun getDeleteDefaultValueFix(element: PsiElement): DeleteStringByElementTypeFix {
        val name = PlsBundle.message("inspection.script.unsupportedParameterUsage.fix.1")
        return DeleteStringByElementTypeFix(element, name, ParadoxScriptElementTypes.PIPE, ParadoxScriptElementTypes.PARAMETER_END)
    }
}
