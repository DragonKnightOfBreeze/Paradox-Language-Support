package icu.windea.pls.core.fixes

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

// com.intellij.codeInsight.daemon.impl.quickfix.InsertMissingTokenFix

class InsertMissingTokenFix(
    private val title: String,
    private val token: String,
    private val offset: Int
) : IntentionAction, DumbAware {
    override fun getFamilyName() = title

    override fun getText() = title

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        editor.document.insertString(offset, token)
        editor.caretModel.moveToOffset(offset)
    }

    override fun startInWriteAction() = true
}
