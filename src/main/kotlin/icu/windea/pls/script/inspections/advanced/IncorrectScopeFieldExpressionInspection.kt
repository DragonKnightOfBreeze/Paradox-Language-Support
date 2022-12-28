package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 不正确的作用域字段表达式的检查。
 *
 * @property reportsUnresolvedDs 是否报告无法解析的DS引用。
 */
class IncorrectScopeFieldExpressionInspection : LocalInspectionTool() {
	@JvmField var reportsUnresolvedDs = true
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
		val gameType = selectGameType(file)
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
				val type = config.expression.type
				if(type == CwtDataType.Scope || type == CwtDataType.ScopeField || type == CwtDataType.ScopeGroup) {
					val value = element.value
					val gameTypeToUse = gameType ?: selectGameType(element) ?: return
					val configGroup = getCwtConfig(project).getValue(gameTypeToUse)
					val textRange = TextRange.create(0, value.length)
					val isKey = element is ParadoxScriptPropertyKey
					val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, textRange, configGroup, isKey)
						?: return
					scopeFieldExpression.validate().forEach { error ->
						handleScriptExpressionError(element, error)
					}
					scopeFieldExpression.processAllNodes { node ->
						val unresolvedError = node.getUnresolvedError(element)
						if(unresolvedError != null) {
							handleScriptExpressionError(element, unresolvedError)
						}
						true
					}
				}
			}
			
			private fun handleScriptExpressionError(element: ParadoxScriptStringExpressionElement, error: ParadoxExpressionError) {
				if(reportsUnresolvedDs && error is ParadoxUnresolvedScopeLinkDataSourceExpressionError) return
				holder.registerScriptExpressionError(element, error)
			}
		})
		return holder.resultsArray
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.incorrectScopeFieldExpression.option.reportsUnresolvedDs"))
					.bindSelected(::reportsUnresolvedDs)
					.actionListener { _, component -> reportsUnresolvedDs = component.isSelected }
			}
		}
	}
}
