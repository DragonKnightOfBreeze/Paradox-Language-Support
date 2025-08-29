package icu.windea.pls.lang.refactoring.inline

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.util.CommonRefactoringUtil
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.configContext.inlineScriptHasRecursion
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

class ParadoxInlineScriptInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.inlineScript")

    override fun isEnabledForLanguage(language: Language) = language is ParadoxScriptLanguage

    override fun canInlineElement(element: PsiElement): Boolean {
        run {
            //此内联操作也可以从"inline_script = {...}"中的"inline_script"发起
            if (element.elementType != ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN) return@run
            val contextReferenceElement = element.parentOfType<ParadoxScriptProperty>() ?: return@run
            if (contextReferenceElement.name.lowercase() != ParadoxInlineScriptManager.inlineScriptKey) return@run
            val expressionElement = ParadoxInlineScriptManager.getExpressionElement(contextReferenceElement) ?: return@run
            val expressionElementReference = expressionElement.reference ?: return@run
            val resolved = expressionElementReference.resolve() ?: return@run
            return canInlineElement(resolved)
        }

        if (element !is ParadoxScriptFile) return false
        if (ParadoxInlineScriptManager.getInlineScriptExpression(element) == null) return false
        return true
    }

    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        run {
            //此内联操作也可以从"inline_script = {...}"中的"inline_script"发起
            if (reference == null) return@run
            val contextReferenceElement = reference.element.castOrNull<ParadoxScriptPropertyKey>()?.parent?.castOrNull<ParadoxScriptProperty>() ?: return@run
            if (contextReferenceElement.name.lowercase() != ParadoxInlineScriptManager.inlineScriptKey) return@run
            val expressionElement = ParadoxInlineScriptManager.getExpressionElement(contextReferenceElement) ?: return@run
            val expressionElementReference = expressionElement.reference ?: return@run
            val resolved = expressionElementReference.resolve() ?: return@run
            return canInlineElement(resolved)
        }
        if (reference != null && ParadoxInlineScriptManager.getContextReferenceElement(reference.element) == null) return false
        return canInlineElement(element)
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        run {
            //此内联操作也可以从"inline_script = {...}"中的"inline_script"发起
            if (reference == null) return@run
            val contextReferenceElement = reference.element.castOrNull<ParadoxScriptPropertyKey>()?.parent?.castOrNull<ParadoxScriptProperty>() ?: return@run
            if (contextReferenceElement.name.lowercase() != ParadoxInlineScriptManager.inlineScriptKey) return@run
            val expressionElement = ParadoxInlineScriptManager.getExpressionElement(contextReferenceElement) ?: return@run
            val expressionElementReference = expressionElement.reference ?: return@run
            val resolved = expressionElementReference.resolve() ?: return@run
            return performInline(project, editor, resolved.castOrNull() ?: return, expressionElementReference)
        }
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }

    private fun performInline(project: Project, editor: Editor?, element: ParadoxScriptFile, reference: PsiReference?) {

        val configContext = ParadoxExpressionManager.getConfigContext(element) ?: return //unexpected
        if (configContext.inlineScriptHasRecursion == true) {
            val message = PlsBundle.message("refactoring.inlineScript.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        val dialog = ParadoxInlineScriptInlineDialog(project, element, reference, editor)
        dialog.show()
    }

    private fun getRefactoringName() = PlsBundle.message("title.inline.inlineScript")
}
