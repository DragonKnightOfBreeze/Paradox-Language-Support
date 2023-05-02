package icu.windea.pls.script.inspections

import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 同一文件中重复的封装变量声明的检查。
 *
 * 提供快速修复：
 * * 导航到重复项
 */
class DuplicateScriptedVariablesInspection : LocalInspectionTool() {
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val rootBlock = file.block ?: return null
		val variableGroup : MutableMap<String, MutableList<ParadoxScriptScriptedVariable>> = mutableMapOf()
		rootBlock.acceptChildren(object: PsiRecursiveElementWalkingVisitor() {
			private var inInlineMath = false
			
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptScriptedVariable) {
					val name = element.name
					variableGroup.getOrPut(name) { SmartList() }.add(element)
					return
				}
				if(element is ParadoxScriptInlineMath) {
					inInlineMath = true
				}
				if(inInlineMath || element.isExpressionOrMemberContext()) {
					super.visitElement(element)
				}
			}
			
			override fun elementFinished(element: PsiElement?) {
				if(element is ParadoxScriptInlineMath) {
					inInlineMath = false
				}
			}
		})
		if(variableGroup.isEmpty()) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		for((name, values) in variableGroup) {
			ProgressManager.checkCanceled()
			if(values.size <= 1) continue
			for(value in values) {
				//第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
				val location = value.scriptedVariableName
				holder.registerProblem(location, PlsBundle.message("inspection.script.duplicateScriptedVariables.description", name),
					NavigateToDuplicatesFix(name, value, values)
				)
			}
		}
		return holder.resultsArray
	}
	
	private class NavigateToDuplicatesFix(
		private val key: String,
		element: ParadoxScriptScriptedVariable,
		duplicates: List<ParadoxScriptScriptedVariable>
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		private val pointers = duplicates.map { it.createPointer() }
		
		override fun getText() = PlsBundle.message("inspection.script.duplicateScriptedVariables.quickfix.1")
		
		override fun getFamilyName() = text
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			if(editor == null) return
			//当重复的变量定义只有另外一个时，直接导航即可
			//如果有多个，则需要创建listPopup
			if(pointers.size == 2) {
				val iterator = pointers.iterator()
				val next = iterator.next().element
				val toNavigate = if(next != startElement) next else iterator.next().element
				if(toNavigate != null) navigateTo(editor, toNavigate)
			} else {
				val allElements = pointers.mapNotNull { it.element }.filter { it !== startElement }
				JBPopupFactory.getInstance().createListPopup(object : BaseListPopupStep<ParadoxScriptScriptedVariable>(PlsBundle.message("inspection.script.duplicateScriptedVariables.quickFix.1.popup.header", key), allElements) {
					override fun getIconFor(value: ParadoxScriptScriptedVariable) = value.icon
					
					override fun getTextFor(value: ParadoxScriptScriptedVariable) =
						PlsBundle.message("inspection.script.duplicateScriptedVariables.quickFix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
					
					override fun getDefaultOptionIndex(): Int = 0
					
					override fun isSpeedSearchEnabled(): Boolean = true
					
					override fun onChosen(selectedValue: ParadoxScriptScriptedVariable, finalChoice: Boolean): PopupStep<*>? {
						navigateTo(editor, selectedValue)
						return PopupStep.FINAL_CHOICE
					}
				}).showInBestPositionFor(editor)
			}
		}
		
		override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY
		
		override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
		
		override fun startInWriteAction() = false
		
		override fun availableInBatchMode() = false
		
		private fun navigateTo(editor: Editor, toNavigate: PsiElement) {
			editor.caretModel.moveToOffset(toNavigate.textOffset)
			editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
		}
	}
}
