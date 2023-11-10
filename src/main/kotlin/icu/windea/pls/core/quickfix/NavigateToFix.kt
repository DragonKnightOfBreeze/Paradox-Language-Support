package icu.windea.pls.core.quickfix

import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*

abstract class NavigateToFix(
    val key: String,
    target: PsiElement,
    elements: Collection<PsiElement>,
    val targetInElements: Boolean = false,
) : LocalQuickFixAndIntentionActionOnPsiElement(target) {
    private val pointers = elements.map { it.createPointer() }
    
    override fun getFamilyName() = text
    
    abstract fun getPopupTitle(editor: Editor): String
    
    abstract fun getPopupText(editor: Editor, value: PsiElement): String
    
    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        if(editor == null) return
        val elements = pointers.mapNotNullTo(mutableListOf()) { it.element }
        if(targetInElements) elements.removeIf { it == startElement }
        if(elements.size == 1) {
            val element = elements.single()
            navigateTo(editor, element)
        } else {
            val popup = object : BaseListPopupStep<PsiElement>(getPopupTitle(editor), elements) {
                override fun getIconFor(value: PsiElement) = value.icon
                
                override fun getTextFor(value: PsiElement) = getPopupText(editor, value)
                
                override fun getDefaultOptionIndex(): Int = 0
                
                override fun isSpeedSearchEnabled(): Boolean = true
                
                override fun onChosen(selectedValue: PsiElement, finalChoice: Boolean): PopupStep<*>? {
                    navigateTo(editor, selectedValue)
                    return PopupStep.FINAL_CHOICE
                }
            }
            JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(editor)
        }
    }
    
    private fun navigateTo(editor: Editor, toNavigate: PsiElement) {
        editor.caretModel.moveToOffset(toNavigate.textOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
    }
    
    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY
    
    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
    
    override fun startInWriteAction() = false
    
    override fun availableInBatchMode() = false
}