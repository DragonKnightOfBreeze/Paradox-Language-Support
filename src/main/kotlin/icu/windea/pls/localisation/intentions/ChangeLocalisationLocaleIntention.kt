package icu.windea.pls.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

/**
 * 更改语言区域的意向。
 */
class ChangeLocalisationLocaleIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = PlsBundle.message("localisation.intention.changeLocalisationLocale")
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		if(file.language != ParadoxLocalisationLanguage) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		return originalElement.elementType == ParadoxLocalisationElementTypes.LOCALE_ID
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		if(file.language != ParadoxLocalisationLanguage) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parent
		if(element is ParadoxLocalisationLocale) {
			val values = InternalConfigHandler.getLocales(project)
			JBPopupFactory.getInstance().createListPopup(Popup(element, values)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationLocale,
		values: Array<ParadoxLocaleConfig>
	) : BaseListPopupStep<ParadoxLocaleConfig>(PlsBundle.message("localisation.intention.changeLocalisationLocale.title"), *values) {
		override fun getIconFor(value: ParadoxLocaleConfig) = value.icon
		
		override fun getTextFor(value: ParadoxLocaleConfig) = value.text
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxLocaleConfig, finalChoice: Boolean): PopupStep<*>? {
			runUndoTransparentWriteAction { value.setName(selectedValue.id) }
			return PopupStep.FINAL_CHOICE
		}
	}
}

