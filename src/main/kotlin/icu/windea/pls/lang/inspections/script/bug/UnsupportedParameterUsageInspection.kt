package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.quickfix.DeleteStringByElementTypeFix
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptParameter

/**
 * （对于脚本文件）检查是否在不支持的地方使用了参数。
 */
class UnsupportedParameterUsageInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                checkGeneral(element, holder)
                checkInlineScript(element, holder)
            }
        }
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
