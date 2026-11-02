package icu.windea.pls.core.fixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class ReplaceStringFix(
    element: PsiElement,
    private val title: String,
    private val string: String,
    private val offset: Int,
    private val length: Int,
    private val moveCaretToOffset: Int = -1,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), DumbAware {
    override fun getFamilyName() = title

    override fun getText() = title

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (length <= 0) return
        val document = file.fileDocument
        document.replaceString(offset, offset + length, string)
        if (editor != null && moveCaretToOffset > 0) {
            editor.caretModel.moveToOffset(moveCaretToOffset)
        }
    }

    override fun startInWriteAction() = true
}
