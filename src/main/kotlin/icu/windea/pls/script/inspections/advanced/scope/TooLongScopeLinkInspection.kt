package icu.windea.pls.script.inspections.advanced.scope

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.psi.*

class TooLongScopeLinkInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
				ProgressManager.checkCanceled()
				if(element.text.isLeftQuoted()) return //忽略
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return
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
						val description = PlsBundle.message("script.inspection.scope.tooLongScopeLink.description")
						holder.registerProblem(element, range, description)
					}
					true
				}
			}
		})
		return holder.resultsArray
	}
}