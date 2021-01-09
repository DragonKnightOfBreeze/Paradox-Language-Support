package com.windea.plugin.idea.paradox.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.command.WriteCommandAction.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

object ChangeColorIntention : IntentionAction {
	private val _name = message("paradox.localisation.intention.changeColor")
	private val _title = message("paradox.localisation.intention.changeColor.title")
	
	override fun startInWriteAction() = false
	
	override fun getText() = _name
	
	override fun getFamilyName() = _name
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val element = file.findElementAt(editor.caretModel.offset)?.parent
		return element is ParadoxLocalisationColorfulText
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val element = file.findElementAt(editor.caretModel.offset)?.parent
		if(element is ParadoxLocalisationColorfulText) {
			JBPopupFactory.getInstance().createListPopup(Popup(element, ParadoxColor.values)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationColorfulText,
		values: Array<ParadoxColor>
	) : BaseListPopupStep<ParadoxColor>(_title, *values) {
		override fun getIconFor(value: ParadoxColor) = value.icon
		
		override fun getTextFor(value: ParadoxColor) = value.popupText
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxColor, finalChoice: Boolean): PopupStep<*>? {
			runWriteCommandAction(value.project) { value.name = selectedValue.name }
			return PopupStep.FINAL_CHOICE
		}
	}
}
