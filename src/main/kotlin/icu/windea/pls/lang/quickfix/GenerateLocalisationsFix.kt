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
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.Type

class GenerateLocalisationsFix(
    element: PsiElement,
    private val context: ParadoxLocalisationCodeInsightContext,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val contextName = context.name.or.anonymous()

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText(): String {
        return when (context.type) {
            Type.Definition -> PlsBundle.message("inspection.script.missingLocalisation.fix.1", contextName)
            Type.Modifier -> PlsBundle.message("inspection.script.missingLocalisation.fix.2", contextName)
            Type.LocalisationReference -> PlsBundle.message("inspection.script.missingLocalisation.fix.3", contextName)
            Type.SyncedLocalisationReference -> PlsBundle.message("inspection.script.missingLocalisation.fix.4", contextName)
            Type.Localisation -> PlsBundle.message("inspection.script.missingLocalisation.fix.5", contextName)
            else -> throw IllegalStateException()
        }
    }

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor == null) return
        invokeLater {
            val handler = ParadoxGenerateLocalisationsHandler(context, fromInspection = true)
            handler.invoke(project, editor, file)
        }
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    //true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}
