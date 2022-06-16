package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.ide.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*

//com.intellij.codeInsight.daemon.impl.quickfix.InsertMissingTokenFix

class InsertMissingTokenFix(
	private val token: String,
	private val caretOffset: Int
): IntentionActionWithFixAllOption, LowPriorityAction {
	override fun getText() = IdeBundle.message("quickfix.text.insert.0", token)
	
	override fun getFamilyName() = text
	
	override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
		return true
	}
	
	override fun invoke(project: Project, editor: Editor, file: PsiFile?) {
		editor.caretModel.moveToOffset(caretOffset)
		editor.document.insertString(editor.caretModel.offset, token)
	}
	
	override fun startInWriteAction(): Boolean {
		return true
	}
	
	override fun belongsToMyFamily(action: IntentionActionWithFixAllOption): Boolean {
		return action is InsertMissingTokenFix && action.token == token
	}
}