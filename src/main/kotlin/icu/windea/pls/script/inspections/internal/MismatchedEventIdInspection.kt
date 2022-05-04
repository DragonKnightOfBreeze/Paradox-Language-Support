package icu.windea.pls.script.inspections.internal

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.ex.util.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 不匹配的事件ID的检查。
 *
 * 仅适用于类型为`event`的定义。
 */
class MismatchedEventIdInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val fileInfo = file.fileInfo ?: return null
		if(fileInfo.path.root != "events") return null
		val rootBlock = file.block ?: return null
		val properties = rootBlock.propertyList
		if(properties.isEmpty()) return null //空文件，不进行检查
		if(properties.find { it.name.equals("namespace", true) } == null) return null //没有事件命名空间，不进行检查
		val eventGroup: MutableMap<String, MutableList<ParadoxScriptProperty>> = mutableMapOf()
		var nextNamespace = ""
		for(property in properties) {
			if(property.name.equals("namespace", true)) {
				//如果值不是一个字符串，作为空字符串存到缓存中
				val namespace = property.propertyValue?.value.castOrNull<ParadoxScriptString>()?.stringValue.orEmpty()
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
			val eventIdPrefix = "$namespace."
			for(event in events) {
				val eventIdProperty = event.findProperty("id") ?: continue //没有事件ID，不进行检查
				val eventId = eventIdProperty.propertyValue?.value.castOrNull<ParadoxScriptString>()?.stringValue ?: continue //事件ID不是字符串，不进行检查
				if(!eventId.startsWith(eventIdPrefix)) {
					if(holder == null) holder = ProblemsHolder(manager, file, isOnTheFly)
					holder.registerProblem(
						eventIdProperty, PlsBundle.message("script.inspection.internal.mismatchedEventId.description", eventId, namespace),
						//RenameEventId(eventIdPropValue)
					)
				}
			}
		}
		return holder?.resultsArray
	}
	
	private class RenameEventId(
		element: ParadoxScriptValue
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getFamilyName() = PlsBundle.message("script.inspection.internal.mismatchedEventId.quickFix.1")
		
		override fun getText() = PlsBundle.message("script.inspection.internal.mismatchedEventId.quickFix.1")
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			if(editor == null) return
			val startOffset = startElement.startOffset
			val endOffset = startElement.endOffset
			EditorUtil.setSelectionExpandingFoldedRegionsIfNeeded(editor, startOffset, endOffset) //选中event.id对应的scriptValue
		}
	}
}