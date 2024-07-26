package icu.windea.pls.lang.inspections.script.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

class IncorrectScopeLinkChainInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptStringExpressionElement) visitExpressionElement(element)
            }
            
            private fun visitExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val config = ParadoxExpressionHandler.getConfigs(element).firstOrNull() ?: return
                val dataType = config.expression.type
                val value = element.value
                val textRange = TextRange.create(0, value.length)
                val complexExpression = when {
                    dataType in CwtDataTypeGroups.DynamicValue -> ParadoxDynamicValueExpression.resolve(value, textRange, configGroup, config)
                    dataType in CwtDataTypeGroups.ScopeField -> ParadoxScopeFieldExpression.resolve(value, textRange, configGroup)
                    dataType in CwtDataTypeGroups.ValueField -> ParadoxValueFieldExpression.resolve(value, textRange, configGroup)
                    dataType in CwtDataTypeGroups.VariableField -> ParadoxVariableFieldExpression.resolve(value, textRange, configGroup)
                    else -> null
                }
                if(complexExpression == null) return
                checkExpression(element, complexExpression)
            }
            
            fun checkExpression(element: ParadoxExpressionElement, complexExpression: ParadoxComplexExpression) {
                complexExpression.processAllNodes p1@{ node1 ->
                    if(node1 !is ParadoxComplexExpression) return@p1 true
                    val scopeNodes = node1.nodes.filterIsInstance<ParadoxScopeFieldNode>()
                    val max = ParadoxScopeHandler.maxScopeLinkSize
                    val actual = scopeNodes.size
                    if(actual <= max) return@p1 true
                    val offset = ParadoxExpressionHandler.getExpressionOffset(element)
                    val startOffset = offset + scopeNodes.first().rangeInExpression.startOffset
                    val endOffset = offset + scopeNodes.last().rangeInExpression.endOffset
                    val range = TextRange.create(startOffset, endOffset)
                    val description = PlsBundle.message("inspection.script.incorrectScopeLinkChain.desc.1", max, actual)
                    holder.registerProblem(element, range, description)
                    true
                }
            }
        }
    }
}
