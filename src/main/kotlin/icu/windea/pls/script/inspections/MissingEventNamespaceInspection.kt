package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

private val _description = PlsBundle.message("script.inspection.missingEventNamespace.description")

/**
 * 缺失事件命名空间的检查。
 *
 * 仅适用于事件定义文件（直接或间接位于`events`目录下）。
 */
class MissingEventNamespaceInspection : LocalInspectionTool() {
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