package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class IncorrectScopeLinkChainInspection : ScopeInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
                val dataType = config.configExpression.type
                val value = element.value
                val complexExpression = when {
                    dataType in CwtDataTypeSets.DynamicValue -> ParadoxDynamicValueExpression.resolve(value, null, configGroup, config)
                    dataType in CwtDataTypeSets.ScopeField -> ParadoxScopeFieldExpression.resolve(value, null, configGroup)
                    dataType in CwtDataTypeSets.ValueField -> ParadoxValueFieldExpression.resolve(value, null, configGroup)
                    dataType in CwtDataTypeSets.VariableField -> ParadoxVariableFieldExpression.resolve(value, null, configGroup)
                    else -> null
                }
                if (complexExpression == null) return
                checkExpression(holder, element, complexExpression)
            }
        }
    }

    private fun checkExpression(holder: ProblemsHolder, element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
        complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression) doCheckExpression(holder, element, node)
                return super.visit(node)
            }
        })
    }

    private fun doCheckExpression(holder: ProblemsHolder, element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
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
