package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptParameter

/**
 * （脚本文件中的）不支持的参数的代码检查。
 *
 * 规则如下：
 * - 仅支持在支持参数的定义声明中，或者内联脚本文件中使用参数。
 * - 不支持在内联脚本文件中使用带默认值的参数。
 */
class UnsupportedParameterInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
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
        holder.registerProblem(element, ChronicleBundle.message("inspection.script.unsupportedParameter.desc.1"))
    }

    private fun checkInlineScript(element: PsiElement, holder: ProblemsHolder) {
        if (element !is ParadoxScriptParameter) return
        if (element.defaultValue == null) return
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        val fix = getDeleteDefaultValueFix(element)
        holder.registerProblem(element, ChronicleBundle.message("inspection.script.unsupportedParameter.desc.2"), fix)
    }

    private fun getDeleteDefaultValueFix(element: PsiElement): DeleteStringByElementTypeFix {
        val name = ChronicleBundle.message("inspection.script.unsupportedParameter.fix.1.name")
        return DeleteStringByElementTypeFix(element, name, ParadoxScriptElementTypes.PIPE, ParadoxScriptElementTypes.PARAMETER_END)
    }
}
