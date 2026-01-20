package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.quickfix.DeleteStringByElementTypeFix
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
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
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
        val name = PlsBundle.message("inspection.script.unsupportedParameterUsage.fix.1.name")
        return DeleteStringByElementTypeFix(element, name, ParadoxScriptElementTypes.PIPE, ParadoxScriptElementTypes.PARAMETER_END)
    }
}
