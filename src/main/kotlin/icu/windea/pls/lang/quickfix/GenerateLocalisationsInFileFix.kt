package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.generation.*

class GenerateLocalisationsInFileFix(
    element: PsiElement,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val fileName = element.containingFile?.name.orAnonymous()

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
