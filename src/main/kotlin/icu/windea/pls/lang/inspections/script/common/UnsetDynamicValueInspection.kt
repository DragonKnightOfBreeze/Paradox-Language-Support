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
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 检查是否存在已被使用但未被设置的动态值。
 *
 * 例如，有 `has_flag = xxx` 但没有 `set_flag = xxx`。
 *
 * 默认不启用。
 */
class UnsetDynamicValueInspection : LocalInspectionTool() {
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
                ParadoxAccessInspectionService.checkForUnsetDynamicValue(element, context)
            }
        }
    }

    private fun createContext(holder: ProblemsHolder): ParadoxAccessInspectionContext {
        return ParadoxAccessInspectionContext(this, holder)
    }
}
