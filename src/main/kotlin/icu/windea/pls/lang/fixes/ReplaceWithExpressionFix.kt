package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
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
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import kotlinx.coroutines.launch

class ReplaceWithExpressionFix(
    element: ParadoxExpressionElement,
    private val replacement: String,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption, PriorityAction, DumbAware {
    override fun getPriority() = PriorityAction.Priority.TOP

    override fun getText() = ChronicleBundle.message("fix.replaceWithExpression.name", replacement)

    override fun getFamilyName() = ChronicleBundle.message("fix.replaceWithExpression.familyName")

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (startElement !is ParadoxExpressionElement) return
        doReplace(project, startElement)
    }

    @Suppress("UnstableApiUsage")
    private fun doReplace(project: Project, element: ParadoxExpressionElement) {
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            writeCommandAction(project, ChronicleBundle.message("fix.replaceWithExpression.command")) {
                element.setValue(replacement)
            }
        }
    }

    override fun availableInBatchMode() = true

    override fun startInWriteAction() = false
}
