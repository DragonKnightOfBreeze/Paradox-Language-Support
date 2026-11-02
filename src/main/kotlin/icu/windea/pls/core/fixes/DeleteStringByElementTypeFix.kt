package icu.windea.pls.core.fixes

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import icu.windea.pls.core.children
import icu.windea.pls.core.select.oneBy

class DeleteStringByElementTypeFix(
    element: PsiElement,
    private val title: String,
    private val startElementType: IElementType? = null,
    private val endElementType: IElementType? = null,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), DumbAware {
    override fun getFamilyName() = title

    override fun getText() = title

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val element = startElement
        if (startElementType == null && endElementType == null) {
            element.delete()
        } else {
            val e1 = element.children(forward = true).oneBy(startElementType) ?: return
            val e2 = element.children(forward = false).oneBy(endElementType) ?: return
            element.deleteChildRange(e1, e2)
        }
    }

    override fun startInWriteAction() = true
}
