package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*

class IncorrectScopeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationExpressionElement) visitExpressionElement(element)
            }
            
            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                if(!element.isCommandExpression()) return
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val complexExpression = ParadoxCommandExpression.resolve(value, textRange, configGroup)
                if(complexExpression == null) return
                checkExpression(element, complexExpression)
            }
            
            fun checkExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                complexExpression.processAllNodes p1@{ node ->
                    if(node is ParadoxComplexExpression) doCheckExpression(element, node)
                    true
                }
            }
            
            private fun doCheckExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                val fieldLinkNode = complexExpression.nodes.lastOrNull()?.castOrNull<ParadoxCommandFieldLinkNode>() ?: return
                val supportedScopes = when(fieldLinkNode) {
                    //parameterized -> skip
                    is ParadoxParameterizedCommandFieldLinkNode -> null
                    //localisation_command
                    is ParadoxPredefinedCommandFieldLinkNode -> fieldLinkNode.config.supportedScopes
                    //scripted_loc or variable -> skip
                    is ParadoxDynamicCommandFieldLinkNode -> null
                    //error -> skip
                    is ParadoxErrorCommandFieldLinkNode -> null
                }
                if(supportedScopes.isNullOrEmpty()) return
                
                val scopeNodes = complexExpression.nodes.filterIsInstance<ParadoxScopeFieldNode>()
                val max = ParadoxScopeHandler.maxScopeLinkSize
                val actual = scopeNodes.size
                if(actual <= max) return
                val offset = ParadoxExpressionHandler.getExpressionOffset(element)
                val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
                val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
                val range = TextRange.create(startOffset, endOffset)
                val description = PlsBundle.message("inspection.localisation.incorrectScopeLinkChain.desc.1", max, actual)
                holder.registerProblem(element, range, description)
            }
        }
    }
}
