package icu.windea.pls.script.inspections

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
		val eventNamespace = file.eventNamespace ?: return null
		val eventIdPrefix = "$eventNamespace."
		val properties = file.properties
		var holder: ProblemsHolder? = null
		for(property in properties) {
			//如果property不能作为定义或者定义类型不为event则跳过
			if(property.definitionInfo?.type != "event") continue
			//如果event定义没有id属性也跳过
			val eventIdProp = property.findProperty("id", true) ?: continue
			val eventIdPropValue = eventIdProp.propertyValue?.value ?: continue
			val eventId = eventIdPropValue.value
			if(!eventId.startsWith(eventIdPrefix, true)) {
				if(holder == null) holder = ProblemsHolder(manager, file, isOnTheFly)
				holder.registerProblem(
					eventIdPropValue, PlsBundle.message("script.inspection.mismatchedEventId.description", eventId, eventNamespace),
					//RenameEventId(eventIdPropValue)
				)
			}
		}
		return holder?.resultsArray
	}
	
	private class RenameEventId(
		element: ParadoxScriptValue
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		override fun getFamilyName() = PlsBundle.message("script.inspection.mismatchedEventId.quickFix.1")
		
		override fun getText() = PlsBundle.message("script.inspection.mismatchedEventId.quickFix.1")
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			if(editor == null) return
			val startOffset = startElement.startOffset
			val endOffset = startElement.endOffset
			EditorUtil.setSelectionExpandingFoldedRegionsIfNeeded(editor, startOffset, endOffset) //选中event.id对应的scriptValue
		}
	}
}