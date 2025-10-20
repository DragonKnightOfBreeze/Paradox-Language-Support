package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class ReplaceWithSimilarExpressionFix(
    element: PsiElement,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText(): @IntentionName String {
        TODO("Not yet implemented")
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        TODO("Not yet implemented")
    }

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        TODO("Not yet implemented")
    }
}
