package com.windea.plugin.idea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.command.WriteCommandAction.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.model.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ChangeLocaleIntention : IntentionAction {
	companion object {
		private val _name = message("paradox.localisation.intention.changeLocale")
		private val _title = message("paradox.localisation.intention.changeLocale.title")
	}
	
	override fun startInWriteAction() = false
	
	override fun getText() = _name
	
	override fun getFamilyName() = _name
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		val element = originalElement.parent
		return element is ParadoxLocalisationLocale
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parent
		if(element is ParadoxLocalisationLocale) {
			JBPopupFactory.getInstance().createListPopup(Popup(element, rule.locales)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationLocale,
		values: Array<ParadoxLocale>
	) : BaseListPopupStep<ParadoxLocale>(_title, *values) {
		override fun getTextFor(value: ParadoxLocale) = value.popupText
		
		override fun getIconFor(value: ParadoxLocale?) = localisationLocaleIcon
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxLocale, finalChoice: Boolean): PopupStep<*>? {
			//需要在WriteCommandAction里面执行
			runWriteCommandAction(value.project) { value.name = selectedValue.name }
			return PopupStep.FINAL_CHOICE
		}
	}
}

