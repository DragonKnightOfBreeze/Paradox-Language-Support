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
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class ParadoxLocalisationInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.localisation")

    override fun isEnabledForLanguage(language: Language) = language is ParadoxLocalisationLanguage

    override fun canInlineElement(element: PsiElement): Boolean {
        if (element !is ParadoxLocalisationProperty) return false
        if (element.name.orNull() == null) return false
        return true
    }

    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        if (reference != null && reference !is ParadoxLocalisationParameterPsiReference) return false
        return super.canInlineElementInEditor(element, editor)
    }

    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        val reference = if (editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        return performInline(project, editor, element.castOrNull() ?: return, reference)
    }

    private fun performInline(project: Project, editor: Editor?, element: ParadoxLocalisationProperty, reference: PsiReference?) {
        if (reference != null && reference !is ParadoxLocalisationParameterPsiReference) {
            val message = PlsBundle.message("refactoring.localisation.reference", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        if (ParadoxLocalisationManager.isSpecialLocalisation(element)) {
            val message = PlsBundle.message("refactoring.localisation.special", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        val isRecursive = ParadoxRecursionManager.isRecursiveLocalisation(element)
        if (isRecursive) {
            val message = PlsBundle.message("refactoring.localisation.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }

        run {
            if (reference == null) return@run
            val referenceElement = reference.element.castOrNull<ParadoxLocalisationParameter>() ?: return@run
            if (referenceElement.argumentElement != null) {
                val message = PlsBundle.message("refactoring.localisation.withArgument", getRefactoringName())
                CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
                return
            }
        }

        val dialog = ParadoxLocalisationInlineDialog(project, element, reference, editor)
        dialog.show()
    }

    private fun getRefactoringName() = PlsBundle.message("title.inline.localisation")
}
