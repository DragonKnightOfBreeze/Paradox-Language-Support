package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的[ParadoxDynamicValueExpression]的检查。
 */
class IncorrectDynamicValueExpressionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return
                val dataType = config.expression.type
                if(dataType !in CwtDataTypeGroups.DynamicValue) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxDynamicValueExpression.resolve(value, textRange, configGroup, config) ?: return
                handleErrors(element, expression)
            }
            
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, expression: ParadoxDynamicValueExpression) {
                expression.errors.forEach { error -> handleError(element, error) }
                expression.processAllNodes { node -> node.getUnresolvedError(element)?.let { error -> handleError(element, error) }.let { true } }
            }
            
            private fun handleError(element: ParadoxScriptStringExpressionElement, error: ParadoxComplexExpressionError) {
                holder.registerExpressionError(error, element)
            }
        }
    }
}

