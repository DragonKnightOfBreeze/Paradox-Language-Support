package icu.windea.pls.localisation.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

/**
 * 无法解析的图标的检查。
 *
 * @property ignoredIconNames （配置项）需要忽略的图标名的模式。使用GLOB模式。忽略大小写。默认为"mod_.*"，以忽略生成的修饰符对应的图标。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
	@JvmField var ignoredIconNames = ""
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: UnresolvedIconInspection,
		private val holder: ProblemsHolder
	) : ParadoxLocalisationVisitor() {
		override fun visitIcon(element: ParadoxLocalisationIcon) {
			ProgressManager.checkCanceled()
			val iconName = element.name ?: return
			if(iconName.matchesGlobFileName(inspection.ignoredIconNames, true)) return //忽略
			val resolved = element.reference?.resolve()
			if(resolved != null) return
			val location = element.iconId ?: return
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.advanced.unresolvedIcon.description", iconName), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
				ImportGameOrModDirectoryFix(location)
			)
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("localisation.inspection.advanced.unresolvedIcon.option.ignoredIconNames"))
			}
			row {
				textField()
					.bindText(::ignoredIconNames)
					.applyToComponent {
						whenTextChanged {
							val document = it.document
							val text = document.getText(0, document.length)
							if(text != ignoredIconNames) ignoredIconNames = text
						}
					}
					.comment(PlsBundle.message("localisation.inspection.advanced.unresolvedIcon.option.ignoredIconNames.comment"))
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
			}
		}
	}
} 
