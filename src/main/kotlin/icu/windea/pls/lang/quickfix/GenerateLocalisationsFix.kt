package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.generation.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*

class GenerateLocalisationsFix(
    element: PsiElement,
    private val context: ParadoxLocalisationCodeInsightContext,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val contextName = context.name.orAnonymous()

    override fun getPriority() = PriorityAction.Priority.HIGH

    override fun getText(): String {
        return when (context.type) {
            Type.Definition -> PlsBundle.message("inspection.script.missingLocalisation.fix.1", contextName)
            Type.Modifier -> PlsBundle.message("inspection.script.missingLocalisation.fix.2", contextName)
            Type.LocalisationReference -> PlsBundle.message("inspection.script.missingLocalisation.fix.3", contextName)
            Type.SyncedLocalisationReference -> PlsBundle.message("inspection.script.missingLocalisation.fix.4", contextName)
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

    //true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}
