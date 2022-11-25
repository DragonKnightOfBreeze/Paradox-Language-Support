package icu.windea.pls.script.inspections.advanced.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

class IncorrectValueSetValueExpressionInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
		val gameType = ParadoxSelectorUtils.selectGameType(file)
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : ParadoxScriptRecursiveElementWalkingVisitor() {
			override fun visitPropertyKey(element: ParadoxScriptPropertyKey) {
				visitExpressionElement(element)
			}
			
			override fun visitString(element: ParadoxScriptString) {
				visitExpressionElement(element)
			}
			
			override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
				ProgressManager.checkCanceled()
				if(element.isQuoted()) return //忽略
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return
				val type = config.expression.type
				if(type == CwtDataTypes.Scope || type == CwtDataTypes.ScopeField || type == CwtDataTypes.ScopeGroup) {
					val value = element.value
					val gameTypeToUse = gameType ?: ParadoxSelectorUtils.selectGameType(element) ?: return
					val configGroup = getCwtConfig(project).getValue(gameTypeToUse)
					val textRange = TextRange.create(0, value.length)
					val isKey = element is ParadoxScriptPropertyKey
					val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(value, textRange, config.expression, configGroup, isKey)
						?: return
					valueSetValueExpression.processAllNodes { node ->
						for(error in node.errors) {
							handleScriptExpressionError(element, error, valueSetValueExpression)
						}
						val unresolvedError = node.getUnresolvedError(element)
						if(unresolvedError != null) {
							handleScriptExpressionError(element, unresolvedError, valueSetValueExpression)
						}
						true
					}
				}
			}
			
			private fun handleScriptExpressionError(element: ParadoxScriptExpressionElement, error: ParadoxExpressionError, expression: ParadoxValueSetValueExpression) {
				holder.registerScriptExpressionError(element, error, expression)
			}
		})
		return holder.resultsArray
	}
}
