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
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.lang.util.*

class ParadoxScriptedEffectInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.scriptedEffect")
    
    override fun isEnabledForLanguage(language: Language) = language == ParadoxScriptLanguage
    
    override fun canInlineElement(element: PsiElement): Boolean {
        if(element !is ParadoxScriptProperty) return false
        val definitionInfo = element.definitionInfo ?: return false
        if(definitionInfo.name.orNull() == null) return false
        if(definitionInfo.type != "scripted_effect") return false
        return true
    }
    
    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        if(reference != null && !ParadoxPsiManager.isInvocationReference(element, reference.element)) return false
        return super.canInlineElementInEditor(element, editor)
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }
    
    private fun performInline(project: Project, editor: Editor?, element: ParadoxScriptProperty, reference: PsiReference?) {
        if(reference != null && !ParadoxPsiManager.isInvocationReference(element, reference.element)) {
            val message = PlsBundle.message("refactoring.scriptedEffect.invocation", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        
        val isRecursive = ParadoxRecursionManager.isRecursiveDefinition(element) { _, re -> ParadoxPsiManager.isInvocationReference(element, re) }
        if(isRecursive) {
            val message = PlsBundle.message("refactoring.scriptedEffect.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        val dialog = ParadoxScriptedEffectInlineDialog(project, element, reference, editor)
        dialog.show()
    }
    
    private fun getRefactoringName() = PlsBundle.message("title.inline.scriptedEffect")
}
