package icu.windea.pls.script.inspections.advanced.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 不正确的作用域值表达式的检查。
 *
 * @property reportsUnresolvedDs 是否报告无法解析的DS引用。
 */
class IncorrectScopeFieldExpressionInspection : LocalInspectionTool() {
	@JvmField var reportsUnresolvedDs = true
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
		val gameType = ParadoxSelectorUtils.selectGameType(file)
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : ParadoxScriptRecursiveExpressionElementWalkingVisitor() {
			override fun visitExpressionElement(element: ParadoxExpressionAwareElement) {
				ProgressManager.checkCanceled()
				val config = ParadoxCwtConfigHandler.resolveConfig(element) ?: return
				val type = config.expression.type
				if(type == CwtDataTypes.Scope || type == CwtDataTypes.ScopeField || type == CwtDataTypes.ScopeGroup) {
					if(element.isQuoted()) {
						//不允许用括号括起
						holder.registerProblem(element, PlsBundle.message("script.inspection.expression.scopeField.quoted"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
					} else {
						val value = element.value
						val gameTypeToUse = gameType ?: ParadoxSelectorUtils.selectGameType(element) ?: return
						val configGroup = getCwtConfig(project).getValue(gameTypeToUse)
						val expression = ParadoxScriptExpression.resolveScopeField(value, configGroup)
						if(expression.isEmpty()) {
							//无法解析
							holder.registerProblem(element, PlsBundle.message("script.inspection.expression.scopeField.malformed", value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
						} else {
							for(error in expression.errors) {
								holder.registerScriptExpressionError(element, error)
							}
							//注册无法解析的异常
							if(expression.infos.isNotEmpty()) {
								for(info in expression.infos) {
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
				checkBox(PlsBundle.message("script.inspection.expression.incorrectScopeFieldExpression.option.reportsUnresolvedDs"))
					.bindSelected(::reportsUnresolvedDs)
					.actionListener { _, component -> reportsUnresolvedDs = component.isSelected }
			}
		}
	}
}