package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.ide.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*

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
