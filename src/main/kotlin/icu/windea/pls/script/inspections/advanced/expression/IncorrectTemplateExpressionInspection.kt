package icu.windea.pls.script.inspections.advanced.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.script.psi.*

class IncorrectTemplateExpressionInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                if(dataType == CwtDataType.TemplateExpression) {
                    val value = element.value
                    val textRange = TextRange.create(0, value.length)
                    val isKey = element is ParadoxScriptPropertyKey
                    val template = CwtTemplateExpression.resolve(config.expression.expressionString)
                    val templateExpression = ParadoxTemplateExpression.resolve(value, textRange, template, configGroup, isKey)
                    if(templateExpression == null) return
                    handleErrors(element, templateExpression)
                }
            }
            
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, templateExpression: ParadoxTemplateExpression) {
                templateExpression.validate().forEach { error ->
                    handleError(element, error)
                }
                templateExpression.processAllNodes { node ->
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
        })
        return holder.resultsArray
    }
}