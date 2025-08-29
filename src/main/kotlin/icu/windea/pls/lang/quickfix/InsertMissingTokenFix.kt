package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.ide.IdeBundle
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

//com.intellij.codeInsight.daemon.impl.quickfix.InsertMissingTokenFix

class InsertMissingTokenFix(
    private val token: String,
    private val caretOffset: Int
) : IntentionAction, DumbAware {
    override fun getText() = IdeBundle.message("quickfix.text.insert.0", token)

    override fun getFamilyName() = text

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        editor.caretModel.moveToOffset(caretOffset)
        editor.document.insertString(caretOffset, token)
    }

    override fun startInWriteAction() = true
}
