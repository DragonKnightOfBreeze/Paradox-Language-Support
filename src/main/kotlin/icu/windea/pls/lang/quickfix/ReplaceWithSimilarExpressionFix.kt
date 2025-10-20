package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import kotlinx.coroutines.launch

class ReplaceWithSimilarExpressionFix(
    element: ParadoxExpressionElement,
    private val replacement: String,
    private val mostSimilar: Boolean,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText() = PlsBundle.message("fix.replaceWithSimilarExpression.name", replacement)

    override fun getFamilyName() = if (mostSimilar) PlsBundle.message("fix.replaceWithSimilarExpression.familyName") else text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (startElement !is ParadoxExpressionElement) return
        doReplace(project, startElement)
    }

    @Suppress("UnstableApiUsage")
    private fun doReplace(project: Project, element: ParadoxExpressionElement) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            writeCommandAction(project, PlsBundle.message("fix.replaceWithSimilarExpression.command")) {
                element.setValue(replacement)
            }
        }
    }

    override fun availableInBatchMode() = mostSimilar

    override fun startInWriteAction() = false
}
