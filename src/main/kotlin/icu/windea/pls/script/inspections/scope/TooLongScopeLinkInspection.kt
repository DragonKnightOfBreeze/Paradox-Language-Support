package icu.windea.pls.script.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.complex.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class TooLongScopeLinkInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if(element.text.isLeftQuoted()) return //忽略
                val config = CwtConfigHandler.getConfigs(element).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                when {
                    dataType in CwtDataTypeGroups.ScopeField -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, textRange, configGroup) ?: return
                        checkExpression(element, scopeFieldExpression)
                    }
                    dataType in CwtDataTypeGroups.ValueField -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val valueFieldExpression = ParadoxValueFieldExpression.resolve(value, textRange, configGroup) ?: return
                        checkExpression(element, valueFieldExpression)
                    }
                    dataType in CwtDataTypeGroups.VariableField -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val valueFieldExpression = ParadoxVariableFieldExpression.resolve(value, textRange, configGroup) ?: return
                        checkExpression(element, valueFieldExpression)
                    }
                    dataType in CwtDataTypeGroups.DynamicValue -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(value, textRange, configGroup, config) ?: return
                        checkExpression(element, dynamicValueExpression)
                    }
                }
            }
            
            fun checkExpression(element: ParadoxScriptStringExpressionElement, expression: ParadoxComplexExpression) {
                expression.processAllNodes { node ->
                    val scopeNodes = when {
                        node is ParadoxScopeFieldExpression -> {
                            node.scopeNodes
                        }
                        node is ParadoxValueFieldExpression -> {
                            node.scopeNodes
                        }
                        else -> emptyList()
                    }
                    if(scopeNodes.size > ParadoxScopeHandler.maxScopeLinkSize) {
                        val startOffset = scopeNodes.first().rangeInExpression.startOffset
                        val endOffset = scopeNodes.last().rangeInExpression.endOffset
                        val range = TextRange.create(startOffset, endOffset)
                        val description = PlsBundle.message("inspection.script.scope.tooLongScopeLink.description")
                        holder.registerProblem(element, range, description)
                    }
                    true
                }
            }
        }
    }
}