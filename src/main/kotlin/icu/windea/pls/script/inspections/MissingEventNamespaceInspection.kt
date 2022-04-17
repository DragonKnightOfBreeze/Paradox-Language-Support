package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 缺失事件命名空间的检查。
 *
 * 仅适用于（直接或间接）位于events目录下的定义文件。
 */
class MissingEventNamespaceInspection : LocalInspectionTool() {
	companion object {
		private val _description = PlsBundle.message("script.inspection.missingEventNamespace.description")
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val fileInfo = file.fileInfo ?: return null
		if(!fileInfo.path.parent.startsWith("events")) return null
		val eventNamespace = file.eventNamespace
		if(eventNamespace == null) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			holder.registerProblem(file, _description)
			return holder.resultsArray
		}
		return null
	}
}