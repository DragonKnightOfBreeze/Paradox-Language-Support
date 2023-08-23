package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*
import icu.windea.pls.script.inspections.general.*

class GenerateLocalisationsFix(
    element: PsiElement,
    private val context: ParadoxLocalisationCodeInsightContext,
    private val inspection: MissingLocalisationInspection? = null,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val contextName = context.name.orAnonymous()
    
    override fun getPriority() = PriorityAction.Priority.HIGH
    
    override fun getText(): String {
        return when(context.type) {
            Type.Definition -> PlsBundle.message("inspection.script.general.missingLocalisation.quickfix.1", contextName)
            Type.Modifier -> PlsBundle.message("inspection.script.general.missingLocalisation.quickfix.2", contextName)
            Type.Unresolved -> PlsBundle.message("inspection.script.general.missingLocalisation.quickfix.3", contextName)
            else -> throw IllegalStateException()
        }
    }
    
    override fun getFamilyName() = text
    
    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if(editor == null) return
        val handler = ParadoxGenerateLocalisationsHandler(context, inspection)
        handler.invoke(project, editor, file)
    }
    
    //true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}