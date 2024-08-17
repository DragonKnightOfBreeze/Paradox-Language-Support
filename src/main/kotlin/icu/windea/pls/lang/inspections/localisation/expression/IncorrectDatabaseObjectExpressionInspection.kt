package icu.windea.pls.lang.inspections.localisation.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.lang.expression.complex.*
import javax.swing.*

/**
 * 不正确的[ParadoxDatabaseObjectExpression]的检查。
 *
 * @property reportsUnresolved 是否报告无法解析的引用。
 */
class IncorrectDatabaseObjectExpressionInspection : LocalInspectionTool() {
    @JvmField var reportsUnresolved = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }
            
            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                if(!element.isDatabaseObjectExpression()) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val expression = ParadoxDatabaseObjectExpression.resolve(value, textRange, configGroup) ?: return
                handleErrors(element, expression)
            }
            
            private fun handleErrors(element: ParadoxLocalisationExpressionElement, expression: ParadoxComplexExpression) {
                expression.errors.forEach { error -> handleError(element, error) }
                expression.processAllNodes { node -> node.getUnresolvedError(element)?.let { error -> handleError(element, error) }.let { true } }
            }
            
            private fun handleError(element: ParadoxLocalisationExpressionElement, error: ParadoxComplexExpressionError) {
                if(!reportsUnresolved && error.isUnresolvedError()) return
                holder.registerExpressionError(error, element)
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            //reportsUnresolved
            row {
                checkBox(PlsBundle.message("inspection.localisation.incorrectExpression.option.reportsUnresolved"))
                    .bindSelected(::reportsUnresolved)
                    .actionListener { _, component -> reportsUnresolved = component.isSelected }
            }
        }
    }
}


