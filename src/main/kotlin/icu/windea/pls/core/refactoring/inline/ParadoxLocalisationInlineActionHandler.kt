package icu.windea.pls.core.refactoring.inline

import com.intellij.codeInsight.*
import com.intellij.lang.*
import com.intellij.lang.refactoring.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationInlineActionHandler: InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.localisation")
    
    override fun isEnabledForLanguage(language: Language) = language == ParadoxLocalisationLanguage
    
    override fun canInlineElement(element: PsiElement): Boolean {
        if(element !is ParadoxLocalisationProperty) return false
        if(element.name.orNull() == null) return false
        return true
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        return performInline(project, editor, element.cast())
    }
    
    private fun performInline(project: Project, editor: Editor?, element: ParadoxLocalisationProperty) {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        
        val isRecursive = ParadoxRecursionHandler.isRecursiveLocalisation(element)
        if(isRecursive) {
            val message = PlsBundle.message("refactoring.localisation.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        val dialog = InlineLocalisationDialog(project, element, reference, editor)
        dialog.show()
    }
    
    private fun getRefactoringName() = PlsBundle.message("title.inline.localisation")
}