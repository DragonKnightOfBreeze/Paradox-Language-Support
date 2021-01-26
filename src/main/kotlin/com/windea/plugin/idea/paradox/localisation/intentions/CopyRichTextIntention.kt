package com.windea.plugin.idea.paradox.localisation.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import java.awt.datatransfer.*

class CopyRichTextIntention: IntentionAction {
	companion object {
		private val _name = message("paradox.localisation.intention.copyRichText")
	}
	
	override fun startInWriteAction() = false
	
	override fun getText() = _name
	
	override fun getFamilyName() = _name
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		if(editor == null || file == null) return false
		val originalElement = file.findElementAt(editor.caretModel.offset)?:return false
		val element = originalElement.parentOfType<ParadoxLocalisationProperty>()
		return element != null
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
		if(editor == null || file == null) return
		val originalElement = file.findElementAt(editor.caretModel.offset)?:return
		val element = originalElement.parentOfType<ParadoxLocalisationProperty>() ?: return
		val text = element.renderText()
		CopyPasteManager.getInstance().setContents(StringSelection(text))
	}
}