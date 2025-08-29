package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.lang.expression.complex.ParadoxDynamicValueExpression
import icu.windea.pls.lang.expression.complex.getAllErrors
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 不正确的[ParadoxDynamicValueExpression]的检查。
 */
class IncorrectDynamicValueExpressionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return
                val dataType = config.configExpression.type
                if (dataType !in CwtDataTypeGroups.DynamicValue) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxDynamicValueExpression.resolve(value, textRange, configGroup, config) ?: return
                val errors = expression.getAllErrors(element)
                if (errors.isEmpty()) return
                errors.forEach { error -> error.register(element, holder) }
            }
        }
    }
}

