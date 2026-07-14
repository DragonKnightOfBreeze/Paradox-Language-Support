package icu.windea.pls.lang.fixes.navigation

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.icon

abstract class NavigateToFix(
    target: PsiElement,
    elements: Collection<PsiElement>,
    private val excludeTargetInElements: Boolean = false
) : LocalQuickFixAndIntentionActionOnPsiElement(target) {
    private val elementPointers = elements.map { it.createPointer() }

    override fun getText() = familyName

    abstract fun getPopupTitle(editor: Editor): String

    abstract fun getPopupText(editor: Editor, value: PsiElement): String

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if (editor == null) return
        val elements = elementPointers.mapNotNullTo(mutableListOf()) { it.element }
        if (excludeTargetInElements) elements.removeIf { it == startElement }
        if (elements.isEmpty()) return
        if (elements.size == 1) {
            navigateTo(editor, elements.first())
            return
        }

        val popup = object : BaseListPopupStep<PsiElement>(getPopupTitle(editor), elements) {
            override fun getIconFor(value: PsiElement) = value.icon

            override fun getTextFor(value: PsiElement) = getPopupText(editor, value)

            override fun getDefaultOptionIndex(): Int = 0

            override fun isSpeedSearchEnabled(): Boolean = true

            override fun onChosen(selectedValue: PsiElement, finalChoice: Boolean) = doFinalStep {
                navigateTo(editor, selectedValue)
            }
        }
        JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(editor)
    }

    private fun navigateTo(editor: Editor, toNavigate: PsiElement) {
        val navigationElement = toNavigate.navigationElement
        if (editor.virtualFile == toNavigate.containingFile.virtualFile) {
            editor.caretModel.moveToOffset(navigationElement.textOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        } else {
            navigationElement.castOrNull<NavigatablePsiElement>()?.navigate(true)
        }
    }

    override fun availableInBatchMode() = false
}
