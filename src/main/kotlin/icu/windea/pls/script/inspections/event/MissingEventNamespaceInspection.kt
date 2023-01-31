package icu.windea.pls.script.inspections.event

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 缺失事件命名空间的检查。
 *
 * 在一个事件脚本文件中，事件命名空间被声明为事件定义之前的名为`"namespace"`的顶级属性。
 *
 * 事件脚本文件指位于`events`目录（及其子目录）下的脚本文件。
 *
 * 注意：兼容同一事件定义文件中多个事件命名空间的情况。
 */
class MissingEventNamespaceInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val fileInfo = file.fileInfo ?: return null
		if(!"events".matchesPath(fileInfo.path.path, acceptSelf = false)) return null
		val rootBlock = file.block ?: return null
		val eventNamespace = rootBlock.findChildOfType<ParadoxScriptProperty> {  it.name.equals("namespace", true) }
		if(eventNamespace == null) {
			val holder = ProblemsHolder(manager, file, isOnTheFly)
			holder.registerProblem(file, PlsBundle.message("inspection.script.event.missingEventNamespace.description"))
			return holder.resultsArray
		}
		return null
	}
}