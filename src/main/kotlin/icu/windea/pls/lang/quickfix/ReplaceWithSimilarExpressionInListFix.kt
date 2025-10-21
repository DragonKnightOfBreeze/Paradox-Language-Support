package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.match.similarity.SimilarityMatchResult
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import kotlinx.coroutines.launch

class ReplaceWithSimilarExpressionInListFix(
    element: PsiElement,
    private val replacements: Collection<SimilarityMatchResult>,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText() = PlsBundle.message("fix.replaceWithSimilarExpressionInList.name")

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (startElement !is ParadoxExpressionElement) return
        val items = replacements.distinct()
        if (items.isEmpty()) return
        if (items.size == 1) {
            doReplace(project, startElement, items.first())
            return
        }
        if (editor == null) return
        val step = object : BaseListPopupStep<SimilarityMatchResult>(PlsBundle.message("fix.replaceWithSimilarExpressionInList.popup.title"), items) {
            override fun getTextFor(value: SimilarityMatchResult) = value.render()

            override fun getDefaultOptionIndex() = 0

            override fun isSpeedSearchEnabled() = true

            override fun onChosen(selectedValue: SimilarityMatchResult, finalChoice: Boolean) = doFinalStep {
                doReplace(project, startElement, selectedValue)
            }
        }
        JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(editor)
    }

    @Suppress("UnstableApiUsage")
    private fun doReplace(project: Project, element: ParadoxExpressionElement, replacement: SimilarityMatchResult) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            writeCommandAction(project, PlsBundle.message("fix.replaceWithSimilarExpression.command")) {
                element.setValue(replacement.value)
            }
        }
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun availableInBatchMode() = false

    override fun startInWriteAction() = false
}
