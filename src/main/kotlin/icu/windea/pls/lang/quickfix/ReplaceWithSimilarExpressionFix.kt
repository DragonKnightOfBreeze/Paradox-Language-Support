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
import icu.windea.pls.core.match.similarity.SimilarityMatchResult
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import kotlinx.coroutines.launch

class ReplaceWithSimilarExpressionFix(
    element: ParadoxExpressionElement,
    private val replacement: SimilarityMatchResult,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.TOP

    override fun getText() = PlsBundle.message("fix.replaceWithSimilarExpression.name", replacement)

    override fun getFamilyName() = PlsBundle.message("fix.replaceWithSimilarExpression.familyName")

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (startElement !is ParadoxExpressionElement) return
        doReplace(project, startElement)
    }

    @Suppress("UnstableApiUsage")
    private fun doReplace(project: Project, element: ParadoxExpressionElement) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            writeCommandAction(project, PlsBundle.message("fix.replaceWithSimilarExpression.command")) {
                element.setValue(replacement.value)
            }
        }
    }

    override fun availableInBatchMode() = true

    override fun startInWriteAction() = false
}
