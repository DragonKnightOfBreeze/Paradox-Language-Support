package icu.windea.pls.lang.inspections.localisation.general

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

/**
 * 检查本地化文件中是否包含多个语言区域声明。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件名的模式。使用GLOB模式。忽略大小写。默认为"languages.yml"。
 */
class MultipleLocalesInspection : LocalInspectionTool() {
	@JvmField var ignoredFileNames = "languages.yml"
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<out ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不期望的结果
		if(ParadoxFileManager.isLightFile(file.virtualFile)) return null //不检查临时文件
		if(file.name.matchesGlobFileName(ignoredFileNames, true)) return null //忽略
		if(file.propertyLists.size <= 1) return null //不存在多个语言区域，忽略
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		holder.registerProblem(file, PlsBundle.message("inspection.localisation.multipleLocales.description"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
		return holder.resultsArray
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("inspection.localisation.multipleLocales.option.ignoredFileNames"))
					.applyToComponent { toolTipText = PlsBundle.message("inspection.localisation.multipleLocales.option.ignoredFileNames.tooltip") }
			}
			row {
				expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
					.bindText(::ignoredFileNames)
					.bindWhenTextChanged(::ignoredFileNames)
					.comment(PlsBundle.message("inspection.localisation.multipleLocales.option.ignoredFileNames.comment"))
					.align(Align.FILL)
					.resizableColumn()
			}
		}
	}
}