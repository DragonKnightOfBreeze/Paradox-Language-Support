package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.isCommandExpression
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement

class IncorrectScopeLinkChainInspection : ScopeInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                ProgressManager.checkCanceled()
                check(element, configGroup, holder)
            }
        }
    }

    private fun check(element: ParadoxLocalisationExpressionElement, configGroup: CwtConfigGroup, holder: ProblemsHolder) {
        if (element.isCommandExpression()) {
            val value = element.value
            val commandExpression = ParadoxCommandExpression.resolve(value, null, configGroup) ?: return
            check(element, commandExpression, holder)
        }
    }

    private fun check(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression, holder: ProblemsHolder) {
        complexExpression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression) checkIn(element, node, holder)
                return super.visit(node)
            }
        })
    }

    private fun checkIn(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression, holder: ProblemsHolder) {
        val scopeNodes = complexExpression.nodes.filterIsInstance<ParadoxCommandScopeNode>()
        val max = ParadoxScopeManager.maxScopeLinkSize
        val actual = scopeNodes.size
        if (actual <= max) return
        val offset = ParadoxExpressionService.getExpressionOffset(element)
        val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
        val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
        val range = TextRange.create(startOffset, endOffset)
        val description = ChronicleBundle.message("inspection.localisation.incorrectScopeLinkChain.desc.1", max, actual)
        holder.registerProblem(element, range, description)
    }
}
