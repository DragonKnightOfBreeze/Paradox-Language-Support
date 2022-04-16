package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class MismatchedEventIdInspection : LocalInspectionTool() {
	companion object {
		private fun _description(id: String, namespace: String) = PlsBundle.message("script.inspection.mismatchedEventId.description", id, namespace)
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val eventNamespace = file.eventNamespace?:return null
		val eventIdPrefix = "$eventNamespace."
		val properties = file.properties
		var holder: ProblemsHolder? = null
		for(property in properties) {
			val type = property.definitionInfo?.type
			if(type != "event") continue //如果property不能作为definition或者definitionType不为event则跳过
			val eventIdProp = property.findProperty("id", true) ?: continue //如果event没有定义id也跳过（属于脚本结构检查）
			val eventIdPropValue = eventIdProp.propertyValue?.value ?: continue
			val eventId = eventIdPropValue.value
			if(!eventId.startsWith(eventIdPrefix, true)) {
				if(holder == null) holder = ProblemsHolder(manager, file, isOnTheFly)
				holder.registerProblem(eventIdPropValue, _description(eventId, eventNamespace))
				//holder.registerProblem(eventIdPropValue, _description(eventId, eventNamespace), RenameEventId(eventIdPropValue))
			}
		}
		return holder?.resultsArray
	}
	
	//private class RenameEventId(
	//	element: ParadoxScriptValue
	//) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
	//	companion object {
	//		private val _name = message("script.quickFix.renameEventId")
	//	}
	//	
	//	override fun getFamilyName() = _name
	//	
	//	override fun getText() = _name
	//	
	//	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
	//		if(editor == null) return
	//		val startOffset = startElement.startOffset
	//		val endOffset = startElement.endOffset
	//		EditorUtil.setSelectionExpandingFoldedRegionsIfNeeded(editor, startOffset, endOffset) //选中event.id对应的scriptValue
	//	}
	//}
}