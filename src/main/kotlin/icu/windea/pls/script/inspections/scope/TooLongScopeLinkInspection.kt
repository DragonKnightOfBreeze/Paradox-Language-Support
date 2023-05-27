package icu.windea.pls.script.inspections.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
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
                val config = ParadoxConfigResolver.getConfigs(element, element is ParadoxScriptValue, true, ParadoxConfigMatcher.Options.Default).firstOrNull() ?: return
                val configGroup = config.info.configGroup
                val dataType = config.expression.type
                when {
                    dataType.isScopeFieldType() -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val isKey = element is ParadoxScriptPropertyKey
                        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, textRange, configGroup, isKey)
                            ?: return
                        checkExpression(element, scopeFieldExpression)
                    }
                    dataType.isValueFieldType() -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val isKey = element is ParadoxScriptPropertyKey
                        val valueFieldExpression = ParadoxValueFieldExpression.resolve(value, textRange, configGroup, isKey)
                            ?: return
                        checkExpression(element, valueFieldExpression)
                    }
                    dataType.isVariableFieldType() -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val isKey = element is ParadoxScriptPropertyKey
                        val valueFieldExpression = ParadoxVariableFieldExpression.resolve(value, textRange, configGroup, isKey)
                            ?: return
                        checkExpression(element, valueFieldExpression)
                    }
                    dataType.isValueSetValueType() -> {
                        val value = element.value
                        val textRange = TextRange.create(0, value.length)
                        val isKey = element is ParadoxScriptPropertyKey
                        val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(value, textRange, config, configGroup, isKey)
                            ?: return
                        checkExpression(element, valueSetValueExpression)
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