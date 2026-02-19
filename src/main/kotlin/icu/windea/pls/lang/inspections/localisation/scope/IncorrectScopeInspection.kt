package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.model.scope.ParadoxScopeContext

class IncorrectScopeInspection : ScopeInspectionBase() {
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
                checkExpression(holder, element, commandExpression, configGroup)
            }
        }
    }

    private fun checkExpression(holder: ProblemsHolder, element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression, configGroup: CwtConfigGroup) {
        var inputScopeContext = ParadoxScopeContext.getAny()
        when (complexExpression) {
            is ParadoxCommandExpression -> {
                for (node in complexExpression.nodes) {
                    when (node) {
                        is ParadoxCommandScopeLinkNode -> {
                            val outputScopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, inputScopeContext)
                            inputScopeContext = outputScopeContext
                        }
                        is ParadoxCommandFieldNode -> {
                            val supportedScopes = ParadoxScopeManager.getSupportedScopes(element, node, inputScopeContext)
                            val matched = ParadoxScopeManager.matchesScope(inputScopeContext, supportedScopes, configGroup)
                            val outputScopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, inputScopeContext)
                            inputScopeContext = outputScopeContext

                            if (supportedScopes.isNullOrEmpty()) continue
                            if (matched) continue
                            val offset = ParadoxExpressionManager.getExpressionOffset(element)
                            val startOffset = offset + node.rangeInExpression.startOffset
                            val endOffset = offset + node.rangeInExpression.endOffset
                            val range = TextRange.create(startOffset, endOffset)
                            val description = PlsBundle.message("inspection.localisation.incorrectScope.desc.1", node.text, supportedScopes.joinToString(), outputScopeContext.scope)
                            holder.registerProblem(element, range, description)
                            break // only reports first problem per complex expression
                        }
                    }
                }
            }
        }
    }
}
