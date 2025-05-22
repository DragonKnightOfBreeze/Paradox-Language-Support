package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*

class DeleteStringByElementTypeFix(
    element: PsiElement,
    private val name: String,
    private val startElementType: IElementType? = null,
    private val endElementType: IElementType? = null,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption, DumbAware {
    override fun getText() = name

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val element = startElement
        if (startElementType == null && endElementType == null) {
            element.delete()
        } else {
            val e1 = element.findChild { it.elementType == startElementType } ?: return
            val e2 = element.findChild(forward = false) { it.elementType == endElementType } ?: return
            element.deleteChildRange(e1, e2)
        }
    }

    override fun startInWriteAction() = true

    override fun belongsToMyFamily(action: IntentionActionWithFixAllOption): Boolean {
        return action is DeleteStringByElementTypeFix && action.name == name
    }
}
