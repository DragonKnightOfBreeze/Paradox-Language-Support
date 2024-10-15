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
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.scriptedVariable")

    override fun isEnabledForLanguage(language: Language) = language.isParadoxLanguage()

    override fun canInlineElement(element: PsiElement): Boolean {
        if (element !is ParadoxScriptScriptedVariable) return false
        if (element.name?.orNull() == null) return false
        return true
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }

    private fun performInline(project: Project, editor: Editor?, element: ParadoxScriptScriptedVariable, reference: PsiReference?) {
        val isRecursive = ParadoxRecursionManager.isRecursiveScriptedVariable(element)
        if (isRecursive) {
            val message = PlsBundle.message("refactoring.scriptedVariable.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        val dialog = ParadoxScriptedVariableInlineDialog(project, element, reference, editor)
        dialog.show()
    }

    private fun getRefactoringName() = PlsBundle.message("title.inline.scriptedVariable")
}

