package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.inspections.ParadoxAccessInspectionContext
import icu.windea.pls.lang.inspections.ParadoxAccessInspectionService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 参数被设值但未被使用的代码检查。
 *
 * 例如：有 `some_effect = {PARAM = some_value}` 但没有 `some_effect = { some_prop = $PARAM$ }`，后者是定义的声明。
 */
class UnusedParameterInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = createContext(holder)
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (element.text.isParameterized()) return // skip if parameterized
                ParadoxAccessInspectionService.checkForUnusedParameter(element, context)
            }

            override fun visitConditionParameter(element: ParadoxConditionParameter) {
                super.visitConditionParameter(element)
                ParadoxAccessInspectionService.checkForUnusedParameter(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxAccessInspectionContext {
        return ParadoxAccessInspectionContext(this, holder)
    }
}
