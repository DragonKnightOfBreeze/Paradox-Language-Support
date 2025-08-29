package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.lang.expression.complex.ParadoxComplexExpression
import icu.windea.pls.lang.expression.complex.ParadoxComplexExpressionVisitor
import icu.windea.pls.lang.expression.complex.ParadoxDynamicValueExpression
import icu.windea.pls.lang.expression.complex.ParadoxScopeFieldExpression
import icu.windea.pls.lang.expression.complex.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.complex.ParadoxVariableFieldExpression
import icu.windea.pls.lang.expression.complex.accept
import icu.windea.pls.lang.expression.complex.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.complex.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

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
