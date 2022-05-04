package icu.windea.pls.script.inspections.internal

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 缺失事件命名空间的检查。
 *
 * 仅适用于事件定义文件（直接或间接位于`events`目录下）。
 */
class MissingEventNamespaceInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val fileInfo = file.fileInfo ?: return null
		if(fileInfo.path.root != "events") return null
		val rootBlock = file.block ?: return null
		val eventNamespace = rootBlock.findChild {  it -> it is ParadoxScriptProperty && it.name.equals("namespace", true) }
		if(eventNamespace == null) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			holder.registerProblem(file, PlsBundle.message("script.inspection.internal.missingEventNamespace.description"))
			return holder.resultsArray
		}
		return null
	}
}