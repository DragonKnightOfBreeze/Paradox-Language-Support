package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*

class InsertStringFix(
    private val string: String,
    private val offset: Int,
    private val moveCaretToOffset: Boolean = false
) : IntentionAction {
    override fun getText() = PlsBundle.message("localisation.annotator.adjacentIcon.fix")
    
    override fun getFamilyName() = text
    
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?) = true
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if(editor == null) return
        if(moveCaretToOffset) {
            editor.caretModel.moveToOffset(offset)
        }
        editor.document.insertString(offset, string)
    }
    
    override fun startInWriteAction() = true
}
