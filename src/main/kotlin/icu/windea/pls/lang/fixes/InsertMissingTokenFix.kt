package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle

// com.intellij.codeInsight.daemon.impl.quickfix.InsertMissingTokenFix

class InsertMissingTokenFix(
    private val token: String,
    private val offset: Int
) : IntentionAction, DumbAware {
    override fun getText() = ChronicleBundle.message("fix.insertMissingToken", token)

    override fun getFamilyName() = text

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile) = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        editor.document.insertString(offset, token)
        editor.caretModel.moveToOffset(offset)
    }

    override fun startInWriteAction() = true
}
