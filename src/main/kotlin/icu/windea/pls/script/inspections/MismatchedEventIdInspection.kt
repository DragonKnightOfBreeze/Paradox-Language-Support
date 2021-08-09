package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class MismatchedEventIdInspection : LocalInspectionTool() {
	companion object {
		private fun _description(id: String, namespace: String) = message("script.inspection.mismatchedEventId.description", id, namespace)
	}
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val rootPath = file.fileInfo?.path?.root?:return null
		if(rootPath != "events") return null //如果不是事件的脚本文件，不做检查
		val block = file.block ?: return null
		val firstProperty = PsiTreeUtil.findChildOfType(block, ParadoxScriptProperty::class.java)
		//判断第一个属性是否名为"namespace"，忽略大小写（如果名为"namespace"但未完成，视为通过检查）
		if(firstProperty == null || !firstProperty.name.equals("namespace", true)) {
			return null //如果缺失namespace则不继续检查
		} else {
			val eventNamespace = firstProperty.value ?: return null //如果namespace未完成也不继续检查
			val eventIdPrefix = "$eventNamespace."
			val properties = block.propertyList
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