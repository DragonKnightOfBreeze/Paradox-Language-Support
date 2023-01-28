package icu.windea.pls.script.inspections.advanced.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 不正确的值字段表达式的检查。
 */
class IncorrectVariableFieldExpressionInspection : LocalInspectionTool() {
    @JvmField var reportsUnresolvedDs = true
    
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
                if(element.text.isLeftQuoted()) return //忽略
                val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                if(dataType.isVariableFieldType()) {
                    val value = element.value
                    val textRange = TextRange.create(0, value.length)
                    val isKey = element is ParadoxScriptPropertyKey
                    val variableFieldExpression = ParadoxVariableFieldExpression.resolve(value, textRange, configGroup, isKey)
                    if(variableFieldExpression == null) return
                    handleErrors(element, variableFieldExpression)
                }
            }
            
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, variableFieldExpression: ParadoxVariableFieldExpression) {
                variableFieldExpression.validate().forEach { error ->
                    handleError(element, error)
                }
                variableFieldExpression.processAllNodes { node ->
                    val unresolvedError = node.getUnresolvedError(element)
                    if(unresolvedError != null) {
                        handleError(element, unresolvedError)
                    }
                    true
                }
            }
            
            private fun handleError(element: ParadoxScriptStringExpressionElement, error: ParadoxExpressionError) {
                if(reportsUnresolvedDs && error is ParadoxUnresolvedValueLinkDataSourceExpressionError) return
                holder.registerScriptExpressionError(element, error)
            }
        })
        return holder.resultsArray
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("script.inspection.expression.incorrectVariableFieldExpression.option.reportsUnresolvedDs"))
                    .bindSelected(::reportsUnresolvedDs)
                    .actionListener { _, component -> reportsUnresolvedDs = component.isSelected }
            }
        }
    }
}