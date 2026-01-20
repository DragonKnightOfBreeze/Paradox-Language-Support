package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression

class IncorrectScopeLinkChainInspection : ScopeInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isCommandExpression()) return
                val value = element.value
                val commandExpression = ParadoxCommandExpression.resolve(value, null, configGroup) ?: return
                checkExpression(holder, element, commandExpression)
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
        val scopeNodes = complexExpression.nodes.filterIsInstance<ParadoxCommandScopeLinkNode>()
        val max = ParadoxScopeManager.maxScopeLinkSize
        val actual = scopeNodes.size
        if (actual <= max) return
        val offset = ParadoxExpressionManager.getExpressionOffset(element)
        val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
        val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
        val range = TextRange.create(startOffset, endOffset)
        val description = PlsBundle.message("inspection.localisation.incorrectScopeLinkChain.desc.1", max, actual)
        holder.registerProblem(element, range, description)
    }
}
