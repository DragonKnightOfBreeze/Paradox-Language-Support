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
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.codeInsight.generation.ParadoxGenerateLocalisationsHandler
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*

class GenerateLocalisationsFix(
    element: PsiElement,
    private val context: ParadoxLocalisationCodeInsightContext,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText(): String {
        val contextName = context.name.or.anonymous()
        return when (context.type) {
            Type.Definition -> PlsBundle.message("fix.generateLocalisations.name.1", contextName)
            Type.Modifier -> PlsBundle.message("fix.generateLocalisations.name.2", contextName)
            Type.LocalisationReference -> PlsBundle.message("fix.generateLocalisations.name.3", contextName)
            Type.SyncedLocalisationReference -> PlsBundle.message("fix.generateLocalisations.name.4", contextName)
            Type.Localisation -> PlsBundle.message("fix.generateLocalisations.name.5", contextName)
            else -> throw IllegalStateException()
        }
    }

    override fun getFamilyName() = PlsBundle.message("fix.generateLocalisations.familyName")

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor == null) return
        invokeLater {
            val handler = ParadoxGenerateLocalisationsHandler(context, fromInspection = true)
            handler.invoke(project, editor, file)
        }
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    // true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}
