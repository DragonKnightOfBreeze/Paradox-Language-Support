@file:Suppress("DuplicatedCode")

package com.windea.plugin.idea.paradox.localisation.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import com.intellij.util.containers.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.groupBy
import kotlin.collections.iterator
import kotlin.collections.mapNotNull

class DuplicatePropertyKeysInspection : LocalInspectionTool() {
	companion object{
		private fun _description(key: String) = message("paradox.localisation.inspection.duplicatePropertyKeys.description", key)
	}
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}

	private class Visitor(private val holder: ProblemsHolder) : ParadoxLocalisationVisitor() {
		override fun visitFile(element: PsiFile) {
			val file = element as? ParadoxLocalisationFile ?: return
			val propertyGroup = file.properties.groupBy { it.name }
			for((key, values) in propertyGroup) {
				if(values.size <= 1) continue
				for(value in values) {
					val quickFix = NavigateToDuplicates(key, value, values)
					//第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
					holder.registerProblem(value.propertyKey, _description(key), quickFix)
				}
			}
		}
	}

	private class NavigateToDuplicates(
		private val key: String,
		property: ParadoxLocalisationProperty,
		duplicates: List<ParadoxLocalisationProperty>
	) : LocalQuickFixAndIntentionActionOnPsiElement(property) {
		private val pointers = ContainerUtil.map(duplicates) { SmartPointerManager.createPointer(it) }

		companion object{
			private val _name = message("paradox.localisation.quickFix.navigateToDuplicates")
			private fun _header(key:String) = message("paradox.localisation.quickFix.navigateToDuplicates.header", key)
			private fun _text(key: String,lineNumber:Int) = message("paradox.localisation.quickFix.navigateToDuplicates.text", key, lineNumber)
		}
		
		override fun getFamilyName() = _name
		
		override fun getText() = _name

		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			if(editor == null) return
			//当重复的属性只有另外一个时，直接导航即可
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
			values: List<ParadoxLocalisationProperty>,
			private val key: String,
			private val editor: Editor
		) : BaseListPopupStep<PsiElement>(_header(key), values) {
			override fun getIconFor(aValue: PsiElement) = paradoxLocalisationPropertyIcon
			
			override fun getTextFor(value: PsiElement) = _text(key,editor.document.getLineNumber(value.textOffset))
			
			override fun getDefaultOptionIndex() = 0
			
			override fun isSpeedSearchEnabled() = true
			
			override fun onChosen(selectedValue: PsiElement, finalChoice: Boolean): PopupStep<*>? {
				navigateToElement(editor, selectedValue)
				return FINAL_CHOICE
			}
		}
	}
}
