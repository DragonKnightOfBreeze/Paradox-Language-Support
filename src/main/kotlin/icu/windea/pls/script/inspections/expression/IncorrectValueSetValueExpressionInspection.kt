package icu.windea.pls.script.inspections.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class IncorrectValueSetValueExpressionInspection : LocalInspectionTool() {
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
                val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                if(dataType.isValueSetValueType()) {
                    val value = element.value
                    val textRange = TextRange.create(0, value.length)
                    val isKey = element is ParadoxScriptPropertyKey
                    val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(value, textRange, config, configGroup, isKey)
                    if(valueSetValueExpression == null) return
                    handleErrors(element, valueSetValueExpression)
                }
            }
            
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, valueSetValueExpression: ParadoxValueSetValueExpression) {
                valueSetValueExpression.validate().forEach { error ->
                    handleError(element, error)
                }
                valueSetValueExpression.processAllNodes { node ->
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

