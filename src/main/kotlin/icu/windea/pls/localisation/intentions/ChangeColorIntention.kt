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
import icu.windea.pls.localisation.psi.*

/**
 * 更改颜色的意向。
 */
class ChangeColorIntention : IntentionAction {
	override fun startInWriteAction() = false
	
	override fun getText() = PlsBundle.message("localisation.intention.changeColor")
	
	override fun getFamilyName() = PlsBundle.message("localisation.intention.changeColor")
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return false
		return originalElement.elementType == ParadoxLocalisationElementTypes.COLOR_ID
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val originalElement = file.findElementAt(editor.caretModel.offset) ?: return
		val element = originalElement.parent
		if(element is ParadoxLocalisationColorfulText) {
			val colorConfigs = InternalConfigHandler.getColors(project)
			JBPopupFactory.getInstance().createListPopup(Popup(element, colorConfigs)).showInBestPositionFor(editor)
		}
	}
	
	private class Popup(
		private val value: ParadoxLocalisationColorfulText,
		values: Array<ParadoxColorConfig>
	) : BaseListPopupStep<ParadoxColorConfig>(PlsBundle.message("localisation.intention.changeColor.title"), *values) {
		override fun getIconFor(value: ParadoxColorConfig) = value.icon
		
		override fun getTextFor(value: ParadoxColorConfig) = value.documentation
		
		override fun getDefaultOptionIndex() = 0
		
		override fun isSpeedSearchEnabled(): Boolean = true
		
		override fun onChosen(selectedValue: ParadoxColorConfig, finalChoice: Boolean): PopupStep<*>? {
			runUndoTransparentWriteAction { value.setName(selectedValue.id) }
			return PopupStep.FINAL_CHOICE
		}
	}
}
