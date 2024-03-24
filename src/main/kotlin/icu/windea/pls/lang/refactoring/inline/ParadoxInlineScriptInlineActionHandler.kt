package icu.windea.pls.lang.refactoring.inline

import com.intellij.codeInsight.*
import com.intellij.lang.*
import com.intellij.lang.refactoring.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxInlineScriptInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.inlineScript")
    
    override fun isEnabledForLanguage(language: Language) = language == ParadoxScriptLanguage
    
    override fun canInlineElement(element: PsiElement): Boolean {
        run {
            //此内联操作也可以从"inline_script = {...}"中的"inline_script"发起
            if(element.elementType != ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN) return@run
            val contextReferenceElement = element.parentOfType<ParadoxScriptProperty>() ?: return@run
            if(contextReferenceElement.name.lowercase() != ParadoxInlineScriptHandler.inlineScriptKey) return@run
            val expressionElement = ParadoxInlineScriptHandler.getExpressionElement(contextReferenceElement) ?: return@run
            val expressionElementReference = expressionElement.reference ?: return@run
            val resolved = expressionElementReference.resolve() ?: return@run
            return canInlineElement(resolved)
        }

        if(element !is ParadoxScriptFile) return false
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(element) == null) return false
        return true
    }
    
    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        run {
            //此内联操作也可以从"inline_script = {...}"中的"inline_script"发起 
            if(reference == null) return@run
            val contextReferenceElement = reference.element.castOrNull<ParadoxScriptPropertyKey>()?.parent?.castOrNull<ParadoxScriptProperty>() ?: return@run
            if(contextReferenceElement.name.lowercase() != ParadoxInlineScriptHandler.inlineScriptKey) return@run
            val expressionElement = ParadoxInlineScriptHandler.getExpressionElement(contextReferenceElement) ?: return@run
            val expressionElementReference = expressionElement.reference ?: return@run
            val resolved = expressionElementReference.resolve() ?: return@run
            return canInlineElement(resolved)
        }
        if(reference != null && ParadoxInlineScriptHandler.getContextReferenceElement(reference.element) == null) return false
        return canInlineElement(element)
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        run {
            //此内联操作也可以从"inline_script = {...}"中的"inline_script"发起 
            if(reference == null) return@run
            val contextReferenceElement = reference.element.castOrNull<ParadoxScriptPropertyKey>()?.parent?.castOrNull<ParadoxScriptProperty>() ?: return@run
            if(contextReferenceElement.name.lowercase() != ParadoxInlineScriptHandler.inlineScriptKey) return@run
            val expressionElement = ParadoxInlineScriptHandler.getExpressionElement(contextReferenceElement) ?: return@run
            val expressionElementReference = expressionElement.reference ?: return@run
            val resolved = expressionElementReference.resolve() ?: return@run
            return performInline(project, editor, resolved.castOrNull() ?: return, expressionElementReference)
        }
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }
    
    private fun performInline(project: Project, editor: Editor?, element: ParadoxScriptFile, reference: PsiReference?) {
        
        val configContext = CwtConfigHandler.getConfigContext(element) ?: return //unexpected
        if(configContext.inlineScriptHasRecursion == true) {
            val message = PlsBundle.message("refactoring.inlineScript.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        val dialog = ParadoxInlineScriptInlineDialog(project, element, reference, editor)
        dialog.show()
    }
    
    private fun getRefactoringName() = PlsBundle.message("title.inline.inlineScript")
}
