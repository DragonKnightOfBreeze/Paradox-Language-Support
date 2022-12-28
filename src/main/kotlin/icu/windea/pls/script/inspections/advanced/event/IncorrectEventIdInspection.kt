package icu.windea.pls.script.inspections.advanced.event

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的事件ID的检查。
 *
 * 具体来说，事件ID的格式应当为`{namespace}.{no}`，其中`{namespace}`是事件的命名空间（忽略大小写），`no`是一个非负整数（忽略作为前缀的0）。
 *
 * 在一个事件脚本文件中，事件ID被声明为事件定义的名为`"id"`的属性，事件命名空间被声明为事件定义之前的名为`"namespace"`的顶级属性。
 *
 * 事件脚本文件指位于`events`目录（及其子目录）下的脚本文件。
 *
 * 注意：兼容同一事件定义文件中多个事件命名空间的情况。
 */
class IncorrectEventIdInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val fileInfo = file.fileInfo ?: return null
		if(!"events".matchesPath(fileInfo.path.path, acceptSelf = false)) return null
		val rootBlock = file.block ?: return null
		val properties = rootBlock.propertyList
		if(properties.isEmpty()) return null //空文件，不进行检查
		if(properties.find { it.name.equals("namespace", true) } == null) return null //没有事件命名空间，不进行检查
		val eventGroup: MutableMap<String, MutableList<ParadoxScriptProperty>> = mutableMapOf() //namespace - eventDefinitions
		var nextNamespace = ""
		ProgressManager.checkCanceled()
		for(property in properties) {
			if(property.name.equals("namespace", true)) {
				//如果值不是一个字符串，作为空字符串存到缓存中
				val namespace = property.propertyValue?.castOrNull<ParadoxScriptString>()?.stringValue.orEmpty()
				nextNamespace = namespace
				eventGroup.getOrPut(namespace) { mutableListOf() }
			} else {
				val definitionInfo = property.definitionInfo ?: continue //不是定义，跳过
				if(definitionInfo.type != "event") continue //不是事件定义，跳过 
				eventGroup.getOrPut(nextNamespace) { mutableListOf() }.add(property)
			}
		}
		var holder: ProblemsHolder? = null
		for((namespace, events) in eventGroup) {
			if(namespace.isEmpty()) continue
			if(events.isEmpty()) continue
			for(event in events) {
				val eventIdString = event.findByPath<ParadoxScriptString>("id") ?: continue //没有事件ID，另行检查
				val eventId = eventIdString.stringValue
				if(!ParadoxEventConfigHandler.isValidEventId(eventId, namespace)) {
					if(holder == null) holder = ProblemsHolder(manager, file, isOnTheFly)
					holder.registerProblem(eventIdString, PlsBundle.message("script.inspection.event.incorrectEventId.description", eventId, namespace))
				}
			}
		}
		return holder?.resultsArray
	}
	
	//private class RenameEventId(
	//	element: ParadoxScriptValue
	//) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
	//	override fun getText() = PlsBundle.message("script.inspection.internal.mismatchedEventId.quickFix.1")
	//	
	//	override fun getFamilyName() = text
	//	
	//	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
	//		if(editor == null) return
	//		val startOffset = startElement.startOffset
	//		val endOffset = startElement.endOffset
	//		EditorUtil.setSelectionExpandingFoldedRegionsIfNeeded(editor, startOffset, endOffset) //选中event.id对应的scriptValue
	//	}
	//}
}
