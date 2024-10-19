package icu.windea.pls.lang.intentions.cwt

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

class UnquoteIdentifierIntention : IntentionAction, DumbAware {
    override fun getFamilyName() = text

    override fun getText() = PlsBundle.message("intention.cwt.unquoteIdentifier")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        return element != null
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) {
            val identifier = it.parent
            val result = when (identifier) {
                is CwtOptionKey -> canUnquote(identifier)
                is CwtPropertyKey -> canUnquote(identifier)
                is CwtString -> canUnquote(identifier)
                else -> false
            }
            if (result) identifier else null
        }
    }

    fun canUnquote(element: PsiElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }

    override fun startInWriteAction() = true
}
