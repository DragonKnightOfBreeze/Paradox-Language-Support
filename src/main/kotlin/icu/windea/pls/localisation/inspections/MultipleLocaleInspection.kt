package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*
import kotlin.properties.*

/**
 * （同一文件中）多个语言区域的检查。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件名，以逗号分隔。默认为"languages.yml"
 */
class MultipleLocaleInspection : LocalInspectionTool() {
	var ignoredFileNames: String by Delegates.observable("languages.yml") { p, o, n ->
		if(o != n) ignoredFileNameList = ignoredFileNames.toCommaDelimitedStringList()
	}
	var ignoredFileNameList = ignoredFileNames.toCommaDelimitedStringList()
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不应该出现
		if(file.propertyLists.size <= 1) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		holder.registerProblem(file, PlsBundle.message("localisation.inspection.multipleLocale.description"), ProblemHighlightType.WARNING)
		return holder.resultsArray
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("localisation.inspection.multipleLocale.option.ignoredFileNames")).applyToComponent {
					toolTipText = PlsBundle.message("localisation.inspection.multipleLocale.option.ignoredFileNames.tooltip")
				}
				textField().bindText({ ignoredFileNames }, { ignoredFileNames = it })
			}
		}
	}
}