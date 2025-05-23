package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*

class DeleteStringFix(
    element: PsiElement,
    private val name: String,
    private val string: String,
    private val offset: Int = 0,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption, DumbAware {
    override fun getText() = name

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val lengthToDelete = string.length
        if (lengthToDelete == 0) return
        val element = startElement
        val offsetToDelete = element.startOffset + offset
        val document = file.fileDocument
        val textToDelete = document.getText(TextRange.from(offsetToDelete, lengthToDelete))
        if (textToDelete != string) return
        document.deleteString(offsetToDelete, lengthToDelete)
    }

    override fun startInWriteAction() = true

    override fun belongsToMyFamily(action: IntentionActionWithFixAllOption): Boolean {
        return action is DeleteStringFix && action.name == name
    }
}
