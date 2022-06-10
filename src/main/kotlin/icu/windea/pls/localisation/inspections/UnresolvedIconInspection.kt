package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.observable.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

/**
 * 无法解析的图标的检查。
 *
 * @property ignoredIconNameRegex （配置项）需要忽略的图标名的正则，忽略大小写。默认为"mod_.*"，以忽略生成的修饰符对应的图标。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
	@OptionTag(converter = RegexIgnoreCaseConverter::class)
	@JvmField var ignoredIconNameRegex = """mod_.*""".toRegex(RegexOption.IGNORE_CASE)
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: UnresolvedIconInspection,
		private val holder: ProblemsHolder
	) : ParadoxLocalisationVisitor() {
		override fun visitIcon(element: ParadoxLocalisationIcon) {
			val iconName = element.name ?: return
			if(inspection.ignoredIconNameRegex.matches(iconName)) return //忽略
			val resolved = element.reference?.resolve()
			if(resolved != null) return
			val location = element.iconId ?: return
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unresolvedIcon.description", iconName), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
				ImportGameOrModDirectoryFix(location)
			)
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("localisation.inspection.unresolvedIcon.option.ignoredIconNameRegex")).applyToComponent {
					toolTipText = PlsBundle.message("localisation.inspection.unresolvedIcon.option.ignoredIconNameRegex.tooltip")
				}
			}
			row {
				textField()
					.bindText({ ignoredIconNameRegex.pattern }, { ignoredIconNameRegex = it.toRegex(RegexOption.IGNORE_CASE) })
					.applyToComponent {
						whenTextChanged {
							val document = it.document
							val text = document.getText(0, document.length)
							if(text != ignoredIconNameRegex.pattern) ignoredIconNameRegex = text.toRegex(RegexOption.IGNORE_CASE)
						}
					}
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
			}
		}
	}
} 