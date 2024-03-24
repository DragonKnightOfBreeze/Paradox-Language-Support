package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*

class InsertStringFix(
    private val name: String,
    private val string: String,
    private val caretOffset: Int,
    private val moveCaretToOffset: Boolean = false
) : IntentionActionWithFixAllOption {
    override fun getText() = name
    
    override fun getFamilyName() = text
    
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) = true
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if(editor == null) return
        if(moveCaretToOffset) {
            editor.caretModel.moveToOffset(caretOffset)
        }
        editor.document.insertString(caretOffset, string)
    }
    
    override fun startInWriteAction() = true
    
    override fun belongsToMyFamily(action: IntentionActionWithFixAllOption): Boolean {
        return action is InsertStringFix && action.string == string
    }
}
