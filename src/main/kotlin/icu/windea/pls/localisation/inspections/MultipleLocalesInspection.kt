package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.observable.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

/**
 * （同一文件中）多个语言区域的检查。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件名，以逗号分隔。默认为"languages.yml"
 */
class MultipleLocalesInspection : LocalInspectionTool() {
	@JvmField var ignoredFileNames = "languages.yml"
	
	private var finalIgnoredFileNames = ignoredFileNames.toCommaDelimitedStringSet(ignoreCase = true)
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不应该出现
		if(finalIgnoredFileNames.contains(file.name)) return null //忽略
		if(file.propertyLists.size <= 1) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		holder.registerProblem(file, PlsBundle.message("localisation.inspection.multipleLocales.description"), ProblemHighlightType.WARNING)
		return holder.resultsArray
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("localisation.inspection.multipleLocales.option.ignoredFileNames")).applyToComponent {
					toolTipText = PlsBundle.message("localisation.inspection.multipleLocales.option.ignoredFileNames.tooltip")
				}
			}
			row {
				textField()
					.bindText({
						ignoredFileNames
					}, {
						ignoredFileNames = it
						finalIgnoredFileNames = it.toCommaDelimitedStringSet(ignoreCase = true)
					})
					.applyToComponent { 
						whenTextChanged {
							val document = it.document
							val text = document.getText(0, document.length)
							if(text != ignoredFileNames){
								ignoredFileNames = text
								finalIgnoredFileNames = text.toCommaDelimitedStringSet(ignoreCase = true)
							}
						}
					}
					.horizontalAlign(HorizontalAlign.FILL)
			}
		}
	}
}