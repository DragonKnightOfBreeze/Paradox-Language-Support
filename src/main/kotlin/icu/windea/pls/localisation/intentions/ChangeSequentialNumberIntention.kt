package icu.windea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.command.WriteCommandAction.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ChangeSequentialNumberIntention : IntentionAction {
	companion object {
		private val _name = message("paradox.localisation.intention.changeSequentialNumber")
		private val _title = message("paradox.localisation.intention.changeSequentialNumber.title")
	}
	
	override fun startInWriteAction() = false
	
	override fun getText() = _name
	
	override fun getFamilyName() = _name
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		val element = originalElement.parent
		return element is ParadoxLocalisationSequentialNumber
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parent
		if(element is ParadoxLocalisationSequentialNumber) {
			JBPopupFactory.getInstance().createListPopup(Popup(element, getConfig().sequentialNumbers)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationSequentialNumber,
		values: Array<ParadoxSequentialNumber>
	) : BaseListPopupStep<ParadoxSequentialNumber>(_title, *values) {
		override fun getTextFor(value: ParadoxSequentialNumber) = value.popupText
		
		override fun getIconFor(value: ParadoxSequentialNumber) = localisationSequentialNumberIcon
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxSequentialNumber, finalChoice: Boolean): PopupStep<*>? {
			//需要在WriteCommandAction里面执行
			runWriteCommandAction(value.project) { value.name = selectedValue.name }
			return PopupStep.FINAL_CHOICE
		}
	}
}
