package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.inspections.PlsInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.isExpression

/**
 * 不正确的表达式的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles 是否在内联脚本文件中忽略此代码检查。
 */
class IncorrectExpressionInspection : LocalInspectionTool() {
    @JvmField
    var ignoredInInjectedFiles = false
    @JvmField
    var ignoredInInlineScriptFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
                if (!element.isExpression()) return // skip check if element is not an expression

                // 跳过一些脚本表达式类型
                if (element is ParadoxScriptBlock) return
                if (element is ParadoxScriptBoolean) return

                // 得到完全匹配的CWT规则
                val config = ParadoxConfigManager.getConfigs(element, orDefault = false).firstOrNull() ?: return

                // 开始检查
                PlsInspectionService.checkIncorrectExpression(element, config, holder)

                // TODO 1.3.26+ 应当也适用于各种复杂表达式中的数据源
            }
        }
    }
}
