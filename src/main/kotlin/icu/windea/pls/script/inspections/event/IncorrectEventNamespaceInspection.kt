package icu.windea.pls.script.inspections.event

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的事件命名空间的检查。
 *
 * 具体来说，事件命名空间应当仅包含字母和下划线。
 *
 * 在一个事件脚本文件中，事件命名空间被声明为事件定义之前的名为`"namespace"`的顶级属性。
 *
 * 事件脚本文件指位于`events`目录（及其子目录）下的脚本文件。
 *
 * 注意：兼容同一事件定义文件中多个事件命名空间的情况。
 */
class IncorrectEventNamespaceInspection  : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val fileInfo = file.fileInfo ?: return null
		if(!"events".matchesPath(fileInfo.path.path, acceptSelf = false)) return null
		val rootBlock = file.block ?: return null
		val properties = rootBlock.propertyList
		if(properties.isEmpty()) return null //空文件，不进行检查
		val namespaceProperties = properties.filter { it.name.equals("namespace", true) }
		if(namespaceProperties.isEmpty()) return null //没有事件命名空间，不进行检查
		var holder: ProblemsHolder? = null
		ProgressManager.checkCanceled()
		for(namespaceProperty in namespaceProperties) {
			ProgressManager.checkCanceled()
			val namespacePropertyValue = namespaceProperty.propertyValue?.castOrNull<ParadoxScriptString>() ?: continue //事件ID不是字符串，另行检查
			val namespace = namespacePropertyValue.stringValue
			if(!ParadoxEventHandler.isValidEventNamespace(namespace)){
				if(holder == null) holder = ProblemsHolder(manager, file, isOnTheFly)
				holder.registerProblem(namespacePropertyValue, PlsBundle.message("inspection.script.event.incorrectEventNamespace.description", namespace))
			}
		}
		return holder?.resultsArray
	}
}
