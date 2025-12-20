package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.children
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.csv.psi.getColumnIndex

object ParadoxCsvManipulator {
    fun findAllColumnsOfIndex(file: PsiFile, index: Int): Sequence<ParadoxCsvColumn> {
        return file.children().filterIsInstance<ParadoxCsvRowElement>().mapNotNull f@{ rowElement ->
            val column = rowElement.children().filterIsInstance<ParadoxCsvColumn>().drop(index).firstOrNull()
            if (column == null || column.getColumnIndex() != index) return@f null
            column
        }
    }

    fun selectElement(editor: Editor, element: PsiElement) {
        val caretModel = editor.caretModel
        val range = element.textRange
        val caret = caretModel.primaryCaret
        caret.moveToOffset(range.startOffset)
        caret.setSelection(range.startOffset, range.endOffset)
        caretModel.removeSecondaryCarets()
    }

    fun selectElements(editor: Editor, elementList: List<PsiElement>) {
        val caretModel = editor.caretModel
        var usePrimary = true
        for (element in elementList) {
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
