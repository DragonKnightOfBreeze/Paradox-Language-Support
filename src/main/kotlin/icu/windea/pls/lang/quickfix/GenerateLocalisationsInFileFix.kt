package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.codeInsight.generation.ParadoxGenerateLocalisationsHandler

class GenerateLocalisationsInFileFix(
    element: PsiElement,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val fileName = element.containingFile?.name.or.anonymous()

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText() = PlsBundle.message("inspection.script.missingLocalisation.fix.0", fileName)

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor == null) return
        invokeLater {
            val handler = ParadoxGenerateLocalisationsHandler(forFile = true, fromInspection = true)
            handler.invoke(project, editor, file)
        }
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    //true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}
