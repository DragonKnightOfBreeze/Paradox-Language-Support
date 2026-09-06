package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isEmpty
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import org.jetbrains.annotations.Nls

class GotoConfigFix(
    element: PsiElement,
    private val name: @Nls String,
    private val config: CwtConfig<*>,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val configElement: NavigatablePsiElement? get() = config.pointer.element.castOrNull()

    override fun getText() = name

    override fun getFamilyName() = ChronicleInspectionBundle.message("fix.gotoConfig.fix")

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun isAvailable(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement): Boolean {
        return !config.pointer.isEmpty()
    }

    override fun invoke(project: Project, psiFile: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val element = configElement ?: return
        element.navigate(true)
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
        val element = configElement ?: return IntentionPreviewInfo.EMPTY
        return IntentionPreviewInfo.navigate(element)
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        val element = configElement ?: return IntentionPreviewInfo.EMPTY
        return IntentionPreviewInfo.navigate(element)
    }

    override fun startInWriteAction() = false

    override fun availableInBatchMode() = false
}
