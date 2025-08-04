package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class IncorrectScopeLinkChainInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return
                val dataType = config.configExpression.type
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val complexExpression = when {
                    dataType in CwtDataTypeGroups.DynamicValue -> ParadoxDynamicValueExpression.resolve(value, textRange, configGroup, config)
                    dataType in CwtDataTypeGroups.ScopeField -> ParadoxScopeFieldExpression.resolve(value, textRange, configGroup)
                    dataType in CwtDataTypeGroups.ValueField -> ParadoxValueFieldExpression.resolve(value, textRange, configGroup)
                    dataType in CwtDataTypeGroups.VariableField -> ParadoxVariableFieldExpression.resolve(value, textRange, configGroup)
                    else -> null
                }
                if (complexExpression == null) return
                checkExpression(element, complexExpression)
            }

            fun checkExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                complexExpression.accept(object : ParadoxComplexExpressionVisitor() {
                    override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
                        if (node is ParadoxComplexExpression) doCheckExpression(element, node)
                        return super.visit(node, parentNode)
                    }
                })
            }

            private fun doCheckExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                val scopeNodes = complexExpression.nodes.filterIsInstance<ParadoxScopeLinkNode>()
                val max = ParadoxScopeManager.maxScopeLinkSize
                val actual = scopeNodes.size
                if (actual <= max) return
                val offset = ParadoxExpressionManager.getExpressionOffset(element)
                val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
                val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
                val range = TextRange.create(startOffset, endOffset)
                val description = PlsBundle.message("inspection.script.incorrectScopeLinkChain.desc.1", max, actual)
                holder.registerProblem(element, range, description)
            }
        }
    }
}
