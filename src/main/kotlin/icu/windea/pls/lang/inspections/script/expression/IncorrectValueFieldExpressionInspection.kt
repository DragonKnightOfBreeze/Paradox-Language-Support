package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的[ParadoxValueFieldExpression]的检查。
 */
class IncorrectValueFieldExpressionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return
                val dataType = config.configExpression.type
                if (dataType !in CwtDataTypeGroups.ValueField) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxValueFieldExpression.resolve(value, textRange, configGroup) ?: return
                val errors = expression.getAllErrors(element)
                if (errors.isEmpty()) return
                errors.forEach { error -> error.register(element, holder) }
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }
}
