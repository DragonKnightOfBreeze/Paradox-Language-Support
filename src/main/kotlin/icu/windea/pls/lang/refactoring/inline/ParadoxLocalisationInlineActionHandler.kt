package icu.windea.pls.lang.refactoring.inline

import com.intellij.codeInsight.*
import com.intellij.lang.*
import com.intellij.lang.refactoring.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.lang.util.*

class ParadoxLocalisationInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.localisation")
    
    override fun isEnabledForLanguage(language: Language) = language == ParadoxLocalisationLanguage
    
    override fun canInlineElement(element: PsiElement): Boolean {
        if(element !is ParadoxLocalisationProperty) return false
        if(element.name.orNull() == null) return false
        return true
    }
    
    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        if(reference != null && reference !is ParadoxLocalisationPropertyPsiReference) return false
        return super.canInlineElementInEditor(element, editor)
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }
    
    private fun performInline(project: Project, editor: Editor?, element: ParadoxLocalisationProperty, reference: PsiReference?) {
        if(reference != null && reference !is ParadoxLocalisationPropertyPsiReference) {
            val message = PlsBundle.message("refactoring.localisation.reference", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        val isRecursive = ParadoxRecursionManager.isRecursiveLocalisation(element)
        if(isRecursive) {
            val message = PlsBundle.message("refactoring.localisation.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        run {
            if(reference == null) return@run
            val referenceElement = reference.element.castOrNull<ParadoxLocalisationPropertyReference>() ?: return@run
            val parameter = referenceElement.propertyReferenceParameter?.text?.orNull() ?: return@run
            if(parameter.singleOrNull()?.isExactLetter() != true) {
                val message = PlsBundle.message("refactoring.localisation.parameter", getRefactoringName())
                CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
                return
            }
        }
        
        val dialog = ParadoxLocalisationInlineDialog(project, element, reference, editor)
        dialog.show()
    }
    
    private fun getRefactoringName() = PlsBundle.message("title.inline.localisation")
}