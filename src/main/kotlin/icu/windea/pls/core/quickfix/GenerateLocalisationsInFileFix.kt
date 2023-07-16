package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.script.psi.*

class GenerateLocalisationsInFileFix(
    private val context: GenerateLocalisationsInFileContext,
    element: ParadoxScriptDefinitionElement
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    override fun getPriority() = PriorityAction.Priority.HIGH
    
    override fun getText() = PlsBundle.message("inspection.script.general.missingLocalisation.quickfix.2", context.fileName)
    
    override fun getFamilyName() = text
    
    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if(editor == null) return
        val handler = GenerateLocalisationsInFileHandler()
        file.putUserData(PlsKeys.generateLocalisationsInFileContext, context)
        handler.invoke(project, editor, file)
        file.putUserData(PlsKeys.generateLocalisationsInFileContext, null)
    }
    
    //true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}