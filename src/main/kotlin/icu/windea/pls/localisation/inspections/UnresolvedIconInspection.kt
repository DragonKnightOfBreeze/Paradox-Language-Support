package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.observable.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.*
import com.intellij.util.xmlb.XmlSerializer
import com.twelvemonkeys.xml.XMLSerializer
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import org.jdom.*
import javax.swing.*

/**
 * 无法解析的图表的检查。
 *
 * @property ignoredIconNameRegex （配置项）需要忽略的图标名的正则。默认为"mod_.*"，以忽略生成的修饰符对应的图标。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
	@JvmField var ignoredIconNameRegex = """mod_.*"""
	
	private var finalIgnoredIConNameRegex = ignoredIconNameRegex.toRegex(RegexOption.IGNORE_CASE)
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: UnresolvedIconInspection,
		private val holder: ProblemsHolder
	) : ParadoxLocalisationVisitor() {
		override fun visitIcon(element: ParadoxLocalisationIcon) {
			val iconName = element.name
			if(inspection.finalIgnoredIConNameRegex.matches(iconName)) return //忽略
			val resolved = element.reference?.resolve()
			if(resolved != null) return
			val location = element.iconId ?: return
			holder.registerProblem(location, PlsBundle.message("localisation.inspection.unresolvedIcon.description", iconName), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
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
					.bindText({
						ignoredIconNameRegex
					}, {
						ignoredIconNameRegex = it
						finalIgnoredIConNameRegex = it.toRegex(RegexOption.IGNORE_CASE)
					})
					.applyToComponent {
						whenTextChanged {
							val document = it.document
							val text = document.getText(0, document.length)
							if(text != ignoredIconNameRegex){
								ignoredIconNameRegex = text
								finalIgnoredIConNameRegex = text.toRegex(RegexOption.IGNORE_CASE)
							}
						}
					}
					.horizontalAlign(HorizontalAlign.FILL)
			}
		}
	}
} 