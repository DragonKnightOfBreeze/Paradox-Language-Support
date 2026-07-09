package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.match.similarity.SimilarityMatchResult
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import kotlinx.coroutines.launch

class ReplaceWithSimilarExpressionFix(
    element: ParadoxExpressionElement,
    private val replacement: SimilarityMatchResult,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction, DumbAware {
    override fun getText() = ChronicleBundle.message("fix.replaceWithSimilarExpression.name", replacement.value)

    override fun getFamilyName() = ChronicleBundle.message("fix.replaceWithSimilarExpression.familyName")

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (startElement !is ParadoxExpressionElement) return
        doReplace(project, startElement)
    }

    @Suppress("UnstableApiUsage")
    private fun doReplace(project: Project, element: ParadoxExpressionElement) {
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            writeCommandAction(project, ChronicleBundle.message("fix.replaceWithSimilarExpression.command")) {
                element.setValue(replacement.value)
            }
        }
    }

    // true here to enable batch replacement
    override fun availableInBatchMode() = true

    override fun startInWriteAction() = false
}
