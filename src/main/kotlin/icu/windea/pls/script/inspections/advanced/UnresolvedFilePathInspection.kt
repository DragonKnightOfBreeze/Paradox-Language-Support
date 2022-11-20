package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveValueConfigs
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的文件路径的检查。
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径的模式。使用ANT模式。忽略大小写。
 */
class UnresolvedFilePathInspection : LocalInspectionTool() {
	@JvmField var ignoredFilePaths = "*.lua"
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: UnresolvedFilePathInspection,
		private val holder: ProblemsHolder
	) : ParadoxScriptVisitor() {
		override fun visitString(valueElement: ParadoxScriptString) {
			ProgressManager.checkCanceled()
			//match or single
			val valueConfig = resolveValueConfigs(valueElement).firstOrNull() ?: return
			val expression = valueConfig.valueExpression
			val project = valueElement.project
			val location = valueElement
			when(expression.type) {
				CwtDataTypes.AbsoluteFilePath -> {
					val filePath = valueElement.value
					val path = filePath.toPathOrNull() ?: return
					if(VfsUtil.findFile(path, false) == null) {
						holder.registerProblem(location, PlsBundle.message("script.inspection.advanced.unresolvedFilePath.description.1", path), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
							ImportGameOrModDirectoryFix(valueElement)
						)
					}
				}
				CwtDataTypes.FilePath -> {
					val expressionType = CwtFilePathExpressionTypes.FilePath
					val filePath = expressionType.resolve(expression.value, valueElement.value.normalizePath())
					if(filePath.matchesAntPath(inspection.ignoredFilePaths, true)) return
					if(findFileByFilePath(filePath, project, selector = fileSelector().gameTypeFrom(valueElement)) == null) {
						holder.registerProblem(location, PlsBundle.message("script.inspection.advanced.unresolvedFilePath.description.2", filePath), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
							ImportGameOrModDirectoryFix(valueElement)
						)
					}
				}
				CwtDataTypes.Icon -> {
					val expressionType = CwtFilePathExpressionTypes.Icon
					val filePath = expressionType.resolve(expression.value, valueElement.value.normalizePath()) ?: return
					if(filePath.matchesAntPath(inspection.ignoredFilePaths, true)) return
					if(findFileByFilePath(filePath, project, selector = fileSelector().gameTypeFrom(valueElement)) == null) {
						holder.registerProblem(location, PlsBundle.message("script.inspection.advanced.unresolvedFilePath.description.3", filePath), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
							ImportGameOrModDirectoryFix(valueElement)
						)
					}
				}
				else -> pass()
			}
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("script.inspection.advanced.unresolvedFilePath.option.ignoredFilePaths"))
			}
			row {
				textField().bindText(::ignoredFilePaths)
					.applyToComponent {
						whenTextChanged {
							val document = it.document
							val text = document.getText(0, document.length)
							if(text != ignoredFilePaths) ignoredFilePaths = text
						}
					}
					.comment(PlsBundle.message("script.inspection.advanced.unresolvedFilePath.option.ignoredFilePaths.comment"))
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
			}
		}
	}
}
