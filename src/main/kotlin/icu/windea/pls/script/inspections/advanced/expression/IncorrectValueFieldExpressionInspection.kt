package icu.windea.pls.script.inspections.advanced.expression

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
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 不正确的值字段表达式的检查。
 *
 * @property reportsUnresolvedDs 是否报告无法解析的DS引用。
 */
class IncorrectValueFieldExpressionInspection : LocalInspectionTool() {
	@JvmField var reportsUnresolvedDs = true
	
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
				if(element.text.isLeftQuoted()) return //忽略
				val config = resolveConfigs(element).firstOrNull() ?: return
				val type = config.expression.type
				if(type == CwtDataTypes.ValueField || type == CwtDataTypes.IntValueField) {
					val value = element.value
					val gameTypeToUse = gameType ?: ParadoxSelectorUtils.selectGameType(element) ?: return
					val configGroup = getCwtConfig(project).getValue(gameTypeToUse)
					val textRange = TextRange.create(0, value.length)
					val isKey = element is ParadoxScriptPropertyKey
					val valueFieldExpression = ParadoxValueFieldExpression.resolve(value, textRange, configGroup, isKey)
						?: return
					valueFieldExpression.validate().forEach { error ->
						handleScriptExpressionError(element, error)
					}
					valueFieldExpression.processAllNodes { node ->
						val unresolvedError = node.getUnresolvedError(element)
						if(unresolvedError != null) {
							handleScriptExpressionError(element, unresolvedError)
						}
						true
					}
				}
			}
			
			private fun handleScriptExpressionError(element: ParadoxScriptExpressionElement, error: ParadoxExpressionError) {
				if(reportsUnresolvedDs && error is ParadoxUnresolvedValueLinkDataSourceExpressionError) return
				holder.registerScriptExpressionError(element, error)
			}
		})
		return holder.resultsArray
	}
	
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				checkBox(PlsBundle.message("script.inspection.expression.incorrectValueFieldExpression.option.reportsUnresolvedDs"))
					.bindSelected(::reportsUnresolvedDs)
					.actionListener { _, component -> reportsUnresolvedDs = component.isSelected }
			}
		}
	}
}

