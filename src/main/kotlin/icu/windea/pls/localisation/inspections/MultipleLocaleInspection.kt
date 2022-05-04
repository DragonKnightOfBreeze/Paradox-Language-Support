package icu.windea.pls.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * （同一文件中）多个语言区域的检查。
 */
class MultipleLocaleInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxLocalisationFile) return null //不应该出现
		if(file.propertyLists.size <= 1) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		holder.registerProblem(file, PlsBundle.message("localisation.inspection.multipleLocale.description"), ProblemHighlightType.WARNING)
		return holder.resultsArray
	}
}