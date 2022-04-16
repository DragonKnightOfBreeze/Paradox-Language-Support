package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class MissingEventNamespaceInspection : LocalInspectionTool() {
	companion object {
		private val _description = PlsBundle.message("script.inspection.missingEventNamespace.description")
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		if(file.fileInfo?.path?.path != "events") return null //认为事件的脚本文件必须直接放到events目录下
		val eventNamespace = file.eventNamespace
		if(eventNamespace == null) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			holder.registerProblem(file, _description)
			return holder.resultsArray
		}
		return null
	}
}