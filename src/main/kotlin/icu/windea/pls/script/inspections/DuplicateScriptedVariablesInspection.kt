package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.quickfix.*
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
					val name = element.name ?: return
					variableGroup.getOrPut(name) { mutableListOf() }.add(element)
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
	
	private class NavigateToDuplicatesFix(key: String, element: PsiElement, duplicates: Collection<PsiElement>): NavigateToFix(key, element, duplicates, true) {
		override fun getText() = PlsBundle.message("inspection.script.duplicateScriptedVariables.quickfix.1")
		
		override fun getPopupTitle(editor: Editor) =
			PlsBundle.message("inspection.script.duplicateScriptedVariables.quickFix.1.popup.title", key)
		
		override fun getPopupText(editor: Editor, value: PsiElement) =
			PlsBundle.message("inspection.script.duplicateScriptedVariables.quickFix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
	} 
}
