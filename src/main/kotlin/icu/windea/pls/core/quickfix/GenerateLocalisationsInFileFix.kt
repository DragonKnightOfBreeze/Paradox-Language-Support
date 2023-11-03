package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.codeInsight.generation.*

class GenerateLocalisationsInFileFix(
    element: PsiElement,
) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
    private val fileName = element.containingFile?.name.orAnonymous()
    
    override fun getPriority() = PriorityAction.Priority.HIGH
    
    override fun getText() = PlsBundle.message("inspection.script.general.missingLocalisation.quickfix.0", fileName)
    
    override fun getFamilyName() = text
    
    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if(editor == null) return
        val handler = ParadoxGenerateLocalisationsHandler(forFile = true, fromInspection = true)
        handler.invoke(project, editor, file)
    }
    
    //true so that we can run MissingLocalisationInspection on mod files scope and generate all missing localisations
    override fun availableInBatchMode() = true
}