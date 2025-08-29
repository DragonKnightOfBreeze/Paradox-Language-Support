package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class InsertStringFix(
    private val name: String,
    private val string: String,
    private val caretOffset: Int,
    private val moveCaretToOffset: Boolean = false
) : IntentionAction, DumbAware {
    override fun getText() = name

    override fun getFamilyName() = text

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        if (moveCaretToOffset) {
            editor.caretModel.moveToOffset(caretOffset)
        }
        editor.document.insertString(caretOffset, string)
    }

    override fun startInWriteAction() = true
}
