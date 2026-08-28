package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.fixes.DeleteStringByElementTypeFix
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes

/**
 * 检查是否在不支持的上下文中使用了参数。
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
        return object : ParadoxPsiElementVisitor() {
            override fun visitParameter(element: ParadoxParameter) {
                super.visitParameter(element)
                checkGeneral(element, holder)
                checkInlineScript(element, holder)
            }

            override fun visitConditionParameter(element: ParadoxScriptConditionParameter) {
                super.visitConditionParameter(element)
                checkGeneral(element, holder)
            }
        }
    }

    private fun checkGeneral(element: ParadoxScriptConditionParameter, holder: ProblemsHolder) {
        if (element.reference?.resolve() != null) return
        holder.registerProblem(element, ChronicleInspectionBundle.message("script.unsupportedParameter.desc.2"))
    }

    private fun checkGeneral(element: ParadoxParameter, holder: ProblemsHolder) {
        if (element.reference?.resolve() != null) return
        holder.registerProblem(element, ChronicleInspectionBundle.message("script.unsupportedParameter.desc.1"))
    }

    private fun checkInlineScript(element: ParadoxParameter, holder: ProblemsHolder) {
        if (element.defaultValue == null) return
        val file = element.containingFile ?: return
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) == null) return
        val fix = getDeleteDefaultValueFix(element)
        holder.registerProblem(element, ChronicleInspectionBundle.message("script.unsupportedParameter.desc.3"), fix)
    }

    private fun getDeleteDefaultValueFix(element: PsiElement): DeleteStringByElementTypeFix {
        val name = ChronicleInspectionBundle.message("script.unsupportedParameter.fix.1.name")
        return DeleteStringByElementTypeFix(element, name, ParadoxScriptElementTypes.PIPE, ParadoxScriptElementTypes.PARAMETER_END)
    }
}
