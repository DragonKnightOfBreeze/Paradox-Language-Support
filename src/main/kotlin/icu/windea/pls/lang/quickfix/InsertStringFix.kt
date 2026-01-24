package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class InsertStringFix(
    element: PsiElement,
    private val name: String,
    private val string: String,
    private val offset: Int,
    private val moveCaretToOffset: Int = -1,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption, DumbAware {
    override fun getText() = name

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (string.isEmpty()) return
        val document = file.fileDocument
        document.insertString(offset, string)
        if (editor != null && moveCaretToOffset > 0) {
            editor.caretModel.moveToOffset(moveCaretToOffset)
        }
    }

    override fun startInWriteAction() = true

    override fun belongsToMyFamily(action: IntentionActionWithFixAllOption): Boolean {
        return action is InsertStringFix && action.name == name
    }
}
