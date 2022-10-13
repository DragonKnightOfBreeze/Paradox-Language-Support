package icu.windea.pls.script.inspections.advanced.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class IncorrectValueFieldExpressionInspection  : LocalInspectionTool() {
	@JvmField var reportsUnresolvedDs = true
	@JvmField var reportsUnusedSvParam = false
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
		val gameType = ParadoxSelectorUtils.selectGameType(file)
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.acceptChildren(object : ParadoxScriptRecursiveExpressionElementWalkingVisitor() {
			override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
				ProgressManager.checkCanceled()
				val config = ParadoxCwtConfigHandler.resolveConfig(element) ?: return
				val type = config.expression.type
				if(type == CwtDataTypes.ValueField || type == CwtDataTypes.IntValueField) {
					if(element.isQuoted()) {
						//不允许用括号括起
						holder.registerProblem(element, PlsBundle.message("script.inspection.expression.valueField.quoted"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
					} else {
						val value = element.value
						val gameTypeToUse = gameType ?: ParadoxSelectorUtils.selectGameType(element) ?: return
						val configGroup = getCwtConfig(project).getValue(gameTypeToUse)
						val expression = ParadoxScriptValueFieldExpression.resolve(value, configGroup)
						if(expression.isEmpty()) {
							//无法解析
							holder.registerProblem(element, PlsBundle.message("script.inspection.expression.valueField.malformed", value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
						} else {
							for(error in expression.errors) {
								holder.registerScriptExpressionError(element, error)
							}
							//注册无法解析的异常
							if(expression.infos.isNotEmpty()) {
								for(info in expression.infos) {
									if(info is ParadoxScriptSvParameterExpressionInfo) {
										if(reportsUnusedSvParam) {
											if(info.isUnresolved(element, config)) {
												val error = info.getUnresolvedError()
												holder.registerScriptExpressionError(element, error)
											}
										}
										continue
									}
									if(reportsUnresolvedDs) {
										if(info.isUnresolved(element, config)) {
											val error = info.getUnresolvedError()
											if(error != null) holder.registerScriptExpressionError(element, error)
										}
									}
								}
							}
						}
					}
				}
				super.visitExpressionElement(element)
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
			row {
				checkBox(PlsBundle.message("script.inspection.expression.incorrectValueFieldExpression.option.reportsUnusedSvParam"))
					.bindSelected(::reportsUnusedSvParam)
					.actionListener { _, component -> reportsUnusedSvParam = component.isSelected }
			}
		}
	}
}