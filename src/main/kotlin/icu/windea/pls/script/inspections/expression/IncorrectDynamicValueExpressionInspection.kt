package icu.windea.pls.script.inspections.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class IncorrectDynamicValueExpressionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val config = CwtConfigHandler.getConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                if(dataType.isDynamicValueType()) {
                    val value = element.value
                    val textRange = TextRange.create(0, value.length)
                    val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(value, textRange, configGroup, config) ?: return
                    handleErrors(element, dynamicValueExpression)
                }
            }
            
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, dynamicValueExpression: ParadoxDynamicValueExpression) {
                dynamicValueExpression.validate().forEach { error ->
                    handleError(element, error)
                }
                dynamicValueExpression.processAllNodes { node ->
                    val unresolvedError = node.getUnresolvedError(element)
                    if(unresolvedError != null) {
                        handleError(element, unresolvedError)
                    }
                    true
                }
            }
            
            private fun handleError(element: ParadoxScriptStringExpressionElement, error: ParadoxExpressionError) {
                holder.registerScriptExpressionError(element, error)
            }
        }
    }
}
