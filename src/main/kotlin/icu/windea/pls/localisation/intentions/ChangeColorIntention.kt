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

private val _name = PlsBundle.message("localisation.intention.changeColor")
private val _popupTitle = PlsBundle.message("localisation.intention.changeColor.title")

/**
 * 更改颜色的意向。
 */
class ChangeColorIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = _name
	
	override fun getFamilyName() = _name
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		val element = originalElement.parent
		return element is ParadoxLocalisationColorfulText
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parent
		if(element is ParadoxLocalisationColorfulText) {
			JBPopupFactory.getInstance().createListPopup(Popup(element, getInternalConfig().colors)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationColorfulText,
		values: Array<ParadoxColorConfig>
	) : BaseListPopupStep<ParadoxColorConfig>(_popupTitle, *values) {
		override fun getIconFor(value: ParadoxColorConfig) = value.icon
		
		override fun getTextFor(value: ParadoxColorConfig) = value.popupText
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxColorConfig, finalChoice: Boolean): PopupStep<*>? {
			runWriteAction { value.name = selectedValue.id }
			return PopupStep.FINAL_CHOICE
		}
	}
}
