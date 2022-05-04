package icu.windea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.localisation.psi.*

/**
 * 更改序列数的意向。
 */
class ChangeSequentialNumberIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = PlsBundle.message("localisation.intention.changeSequentialNumber")
	
	override fun getFamilyName() = PlsBundle.message("localisation.intention.changeSequentialNumber")
	
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
			JBPopupFactory.getInstance().createListPopup(Popup(element, getInternalConfig().sequentialNumbers)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationSequentialNumber,
		values: Array<ParadoxSequentialNumberConfig>
	) : BaseListPopupStep<ParadoxSequentialNumberConfig>(PlsBundle.message("localisation.intention.changeSequentialNumber.title"), *values) {
		override fun getIconFor(value: ParadoxSequentialNumberConfig) = value.icon
		
		override fun getTextFor(value: ParadoxSequentialNumberConfig) = value.popupText
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxSequentialNumberConfig, finalChoice: Boolean): PopupStep<*>? {
			runUndoTransparentWriteAction {
				value.name = selectedValue.id
			}
			return PopupStep.FINAL_CHOICE
		}
	}
}
