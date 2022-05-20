package icu.windea.pls.script.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.*
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
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxScriptVisitor() {
		override fun visitFile(file: PsiFile) {
			if(file !is ParadoxScriptFile) return
			val variableGroup = file.variables.groupBy { it.name }
			for((name, values) in variableGroup) {
				if(values.size <= 1) continue
				for(value in values) {
					//第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
					val location = value.variableName
					holder.registerProblem(location, PlsBundle.message("script.inspection.duplicateScriptedVariables.description", name),
						NavigateToDuplicates(name, value, values)
					)
				}
			}
		}
	}
	
	private class NavigateToDuplicates(
		private val key: String,
		element: ParadoxScriptVariable,
		duplicates: List<ParadoxScriptVariable>
	) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
		private val pointers = duplicates.map { it.createPointer() }
		
		override fun getFamilyName() = PlsBundle.message("script.inspection.duplicateScriptedVariables.quickFix.1")
		
		override fun getText() = PlsBundle.message("script.inspection.duplicateScriptedVariables.quickFix.1")
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			if(editor == null) return
			//当重复的变量定义只有另外一个时，直接导航即可
			//如果有多个，则需要创建listPopup
			if(pointers.size == 2) {
				val iterator = pointers.iterator()
				val next = iterator.next().element
				val toNavigate = if(next != startElement) next else iterator.next().element
				navigateToElement(editor, toNavigate)
			} else {
				val allElements = pointers.mapNotNull { it.element }.filter { it !== startElement }
				val popup = Popup(allElements, key, editor)
				JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(editor)
			}
		}
		
		private class Popup(
			values: List<ParadoxScriptVariable>,
			private val key: String,
			private val editor: Editor
		) : BaseListPopupStep<ParadoxScriptVariable>(PlsBundle.message("script.inspection.duplicateScriptedVariables.quickFix.1.popup.header", key), values) {
			override fun getIconFor(value: ParadoxScriptVariable) = value.icon
			
			override fun getTextFor(value: ParadoxScriptVariable) = PlsBundle.message("script.inspection.duplicateScriptedVariables.quickFix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
			
			override fun getDefaultOptionIndex(): Int = 0
			
			override fun isSpeedSearchEnabled(): Boolean = true
			
			override fun onChosen(selectedValue: ParadoxScriptVariable, finalChoice: Boolean): PopupStep<*>? {
				navigateToElement(editor, selectedValue)
				return PopupStep.FINAL_CHOICE
			}
		}
	}
}
