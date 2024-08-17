package icu.windea.pls.lang.inspections.localisation.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*

class IncorrectScopeSwitchInspection : LocalInspectionTool() {
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
                val commandExpression = ParadoxCommandExpression.resolve(value, textRange, configGroup)
                if(commandExpression == null) return
                checkExpression(element, commandExpression)
            }
            
            fun checkExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                doCheckExpression(element, complexExpression)
            }
            
            private fun doCheckExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                var inputScopeContext = ParadoxScopeManager.getAnyScopeContext()
                when(complexExpression) {
                    is ParadoxCommandExpression -> {
                        for(node in complexExpression.nodes) {
                            when(node) {
                                is ParadoxCommandScopeLinkNode -> {
                                    val supportedScopes = ParadoxScopeManager.getSupportedScopesOfNode(element, node, inputScopeContext)
                                    val matched = ParadoxScopeManager.matchesScope(inputScopeContext, supportedScopes, configGroup)
                                    val outputScopeContext = ParadoxScopeManager.getSwitchedScopeContextOfNode(element, node, inputScopeContext)
                                    inputScopeContext = outputScopeContext ?: ParadoxScopeManager.getUnknownScopeContext(inputScopeContext)
                                    
                                    if(supportedScopes.isNullOrEmpty() || outputScopeContext == null) continue
                                    if(matched) continue
                                    val offset = ParadoxExpressionManager.getExpressionOffset(element)
                                    val startOffset = offset + node.rangeInExpression.startOffset
                                    val endOffset = offset + node.rangeInExpression.endOffset
                                    val range = TextRange.create(startOffset, endOffset)
                                    val description = PlsBundle.message("inspection.localisation.incorrectScopeSwitch.desc.1", node.text, supportedScopes.joinToString(), outputScopeContext.scope)
                                    holder.registerProblem(element, range, description)
                                    break //only reports first problem per complex expression
                                }
                                is ParadoxCommandFieldNode -> break
                            }
                        }
                    }
                }
            }
        }
    }
}
