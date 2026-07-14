package icu.windea.pls.core.editor

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement

object EditorService {
    fun selectElement(editor: Editor, element: PsiElement) {
        val caretModel = editor.caretModel
        val range = element.textRange
        val caret = caretModel.primaryCaret
        caret.moveToOffset(range.startOffset)
        caret.setSelection(range.startOffset, range.endOffset)
        caretModel.removeSecondaryCarets()
    }

    fun selectElements(editor: Editor, elements: List<PsiElement>) {
        val caretModel = editor.caretModel
        var usePrimary = true
        for (element in elements) {
            val range = element.textRange
            if (usePrimary) {
                val caret = caretModel.primaryCaret
                caret.moveToOffset(range.startOffset)
                caret.setSelection(range.startOffset, range.endOffset)
                caretModel.removeSecondaryCarets()
                usePrimary = false
            } else {
                val caret = caretModel.addCaret(editor.offsetToVisualPosition(range.startOffset), false)
                caret?.moveToOffset(range.startOffset)
                caret?.setSelection(range.startOffset, range.endOffset)
            }
        }
    }
}
