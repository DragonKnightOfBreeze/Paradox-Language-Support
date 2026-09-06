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
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import org.jetbrains.annotations.Nls

class GotoTargetFix(
    element: PsiElement,
    private val name: @Nls String,
    private val targetElementPointer: SmartPsiElementPointer<out NavigatablePsiElement>,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val targetElement: NavigatablePsiElement? get() = targetElementPointer.element

    override fun getText() = name

    override fun getFamilyName() = ChronicleInspectionBundle.message("fix.gotoTarget.fix")

    override fun getPriority() = PriorityAction.Priority.TOP // 最高优先级，如果可用

    override fun invoke(project: Project, psiFile: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val element = targetElement ?: return
        element.navigate(true)
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
        val element = targetElement ?: return IntentionPreviewInfo.EMPTY
        return IntentionPreviewInfo.navigate(element)
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        val element = targetElement ?: return IntentionPreviewInfo.EMPTY
        return IntentionPreviewInfo.navigate(element)
    }

    override fun startInWriteAction() = false

    override fun availableInBatchMode() = false
}
