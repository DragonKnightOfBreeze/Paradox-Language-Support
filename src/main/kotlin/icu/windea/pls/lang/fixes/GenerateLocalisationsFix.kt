package icu.windea.pls.lang.fixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext.*
import icu.windea.pls.lang.codeInsight.generation.GenerateLocalisationsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenerateLocalisationsFix(
    element: PsiElement,
    private val context: ParadoxLocalisationCodeInsightContext,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getText(): String {
        val contextName = context.name.or.anonymous()
        return when (context.type) {
            Type.Definition -> ChronicleBundle.message("fix.generateLocalisations.name.1", contextName)
            Type.Modifier -> ChronicleBundle.message("fix.generateLocalisations.name.2", contextName)
            Type.LocalisationReference -> ChronicleBundle.message("fix.generateLocalisations.name.3", contextName)
            Type.SyncedLocalisationReference -> ChronicleBundle.message("fix.generateLocalisations.name.4", contextName)
            Type.Localisation -> ChronicleBundle.message("fix.generateLocalisations.name.5", contextName)
            else -> throw IllegalStateException()
        }
    }

    override fun getFamilyName() = ChronicleBundle.message("fix.generateLocalisations.familyName")

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor == null) return
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            withContext(Dispatchers.EDT) {
                val handler = GenerateLocalisationsHandler(context, fromInspection = true)
                handler.invoke(project, editor, file)
            }
        }
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    // true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}
