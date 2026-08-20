package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.inspections.ParadoxAccessInspectionService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 动态值被设置但未被使用的代码检查。
 *
 * 例如，有 `set_flag = xxx` 但没有 `has_flag = xxx`。
 *
 * 默认不启用。
 */
class UnusedDynamicValueInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val context = ParadoxAccessInspectionService.createContext(this, holder)
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                super.visitStringExpressionElement(element)
                if (element.text.isParameterized()) return // skip if parameterized
                ParadoxAccessInspectionService.checkForUnusedDynamicValue(element, context)
            }
        }
    }
}
