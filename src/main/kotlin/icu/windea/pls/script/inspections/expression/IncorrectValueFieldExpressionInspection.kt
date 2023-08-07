package icu.windea.pls.script.inspections.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 不正确的值字段表达式的检查。
 *
 * @property reportsUnresolvedDs 是否报告无法解析的DS引用。
 */
class IncorrectValueFieldExpressionInspection : LocalInspectionTool() {
    @JvmField var reportsUnresolvedDs = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }
        
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                if(element.text.isLeftQuoted()) return //忽略
                val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                if(dataType.isValueFieldType()) {
                    val value = element.value
                    val textRange = TextRange.create(0, value.length)
                    val valueFieldExpression = ParadoxValueFieldExpression.resolve(value, textRange, configGroup) ?: return
                    handleErrors(element, valueFieldExpression)
                }
            }
        
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, valueFieldExpression: ParadoxValueFieldExpression) {
                valueFieldExpression.validate().forEach { error ->
                    handleError(element, error)
                }
                valueFieldExpression.processAllNodes { node ->
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
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.expression.incorrectValueFieldExpression.option.reportsUnresolvedDs"))
                    .bindSelected(::reportsUnresolvedDs)
                    .actionListener { _, component -> reportsUnresolvedDs = component.isSelected }
            }
        }
    }
}

