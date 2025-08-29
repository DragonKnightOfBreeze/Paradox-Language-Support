package icu.windea.pls.lang.refactoring.inline

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.lang.Language
import com.intellij.lang.refactoring.InlineActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.refactoring.util.CommonRefactoringUtil
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxPsiManager
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptedTriggerInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.scriptedTrigger")

    override fun isEnabledForLanguage(language: Language) = language is ParadoxScriptLanguage

    override fun canInlineElement(element: PsiElement): Boolean {
        if (element !is ParadoxScriptProperty) return false
        val definitionInfo = element.definitionInfo ?: return false
        if (definitionInfo.name.orNull() == null) return false
        if (definitionInfo.type != "scripted_trigger") return false
        return true
    }

    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        if (reference != null && !ParadoxPsiManager.isInvocationReference(element, reference.element)) return false
        return super.canInlineElementInEditor(element, editor)
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }

    private fun performInline(project: Project, editor: Editor?, element: ParadoxScriptProperty, reference: PsiReference?) {
        if (reference != null && !ParadoxPsiManager.isInvocationReference(element, reference.element)) {
            val message = PlsBundle.message("refactoring.scriptedTrigger.invocation", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        val isRecursive = ParadoxRecursionManager.isRecursiveDefinition(element) { _, re -> ParadoxPsiManager.isInvocationReference(element, re) }
        if (isRecursive) {
            val message = PlsBundle.message("refactoring.scriptedTrigger.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        val dialog = ParadoxScriptedTriggerInlineDialog(project, element, reference, editor)
        dialog.show()
    }

    private fun getRefactoringName() = PlsBundle.message("title.inline.scriptedTrigger")
}
