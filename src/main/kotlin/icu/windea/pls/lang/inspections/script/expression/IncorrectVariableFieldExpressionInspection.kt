package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 不正确的[ParadoxVariableFieldExpression]的检查。
 *
 * @property reportsUnresolved 是否报告无法解析的引用。
 */
class IncorrectVariableFieldExpressionInspection : LocalInspectionTool() {
    @JvmField var reportsUnresolved = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val config = ParadoxExpressionHandler.getConfigs(element).firstOrNull() ?: return
                val dataType = config.expression.type
                if(dataType !in CwtDataTypeGroups.VariableField) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxVariableFieldExpression.resolve(value, textRange, configGroup) ?: return
                handleErrors(element, expression)
            }
            
            private fun handleErrors(element: ParadoxScriptStringExpressionElement, expression: ParadoxVariableFieldExpression) {
                expression.validate().forEach { error -> handleError(element, error) }
                expression.processAllNodes { node ->
                    node.getUnresolvedError(element)?.let { error -> handleError(element, error) }
                    true
                }
            }
            
            private fun handleError(element: ParadoxScriptStringExpressionElement, error: ParadoxComplexExpressionError) {
                if(!reportsUnresolved && error.isUnresolvedError()) return
                holder.registerExpressionError(error, element)
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            //reportsUnresolved
            row {
                checkBox(PlsBundle.message("inspection.script.incorrectExpression.option.reportsUnresolved"))
                    .bindSelected(::reportsUnresolved)
                    .actionListener { _, component -> reportsUnresolved = component.isSelected }
            }
        }
    }
}
