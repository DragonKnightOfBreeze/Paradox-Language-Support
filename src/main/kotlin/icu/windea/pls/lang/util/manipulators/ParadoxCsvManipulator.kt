package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import icu.windea.pls.core.children
import icu.windea.pls.core.findElementAt
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.csv.psi.getColumnIndex
import icu.windea.pls.lang.util.dataFlow.ParadoxColumnSequence
import icu.windea.pls.lang.util.dataFlow.ParadoxRowSequence
import icu.windea.pls.lang.util.dataFlow.ParadoxDataFlowOptions.Base as Options

object ParadoxCsvManipulator {
    /**
     * 包含选取范围涉及到的所有行。
     */
    fun buildSelectedRowSequence(editor: Editor, file: PsiFile): ParadoxRowSequence {
        val options = Options()
        val delegate = doBuildSelectedRowSequence(file, editor, options)
        return ParadoxRowSequence(delegate, options)
    }

    private fun doBuildSelectedRowSequence(file: PsiFile, editor: Editor, options: Options): Sequence<ParadoxCsvRow> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            val set = mutableSetOf<ParadoxCsvRow>()
            val allCarets = editor.caretModel.allCarets.let { if (options.forward) it else it.reversed() }
            for (caret in allCarets) {
                val startRow = doFindAndYieldStartRow(caret, file, options, set)
                doFindAndYieldEndRow(caret, startRow, file, options, set)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxCsvRow>.doFindAndYieldStartRow(caret: Caret, file: ParadoxCsvFile, options: Options, set: MutableSet<ParadoxCsvRow>): ParadoxCsvRow? {
        val offset = if (options.forward) caret.selectionStart else caret.selectionEnd
        val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }
        if (row == null) return null
        if (set.add(row)) yield(row)
        return row
    }

    private suspend fun SequenceScope<ParadoxCsvRow>.doFindAndYieldEndRow(caret: Caret, previous: ParadoxCsvRow?, file: ParadoxCsvFile, options: Options, set: MutableSet<ParadoxCsvRow>): ParadoxCsvRow? {
        if (caret.selectionStart == caret.selectionEnd) return null
        val forward = options.forward
        val offset = if (forward) caret.selectionEnd else caret.selectionStart
        val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }?.takeIf { it != previous }
        if (row == null) return null
        val rowsBetween = previous?.siblings(forward = forward, withSelf = false)?.filterIsInstance<ParadoxCsvRow>()?.takeWhile { it != row }
        rowsBetween?.forEach {
            if (set.add(it)) yield(it)
        }
        if (set.add(row)) yield(row)
        return row
    }

    /**
     * 包含选取范围涉及到的，索引在选取开始与选取结束各自对应的列的索引区间中的所有列。
     */
    fun buildSelectedColumnSequence(editor: Editor, file: PsiFile): ParadoxColumnSequence {
        val options = Options()
        val delegate = doBuildSelectedColumnSequence(file, editor, options)
        return ParadoxColumnSequence(delegate, options)
    }

    private fun doBuildSelectedColumnSequence(file: PsiFile, editor: Editor, options: Options): Sequence<ParadoxCsvColumn> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            val set = mutableSetOf<ParadoxCsvColumn>()
            val allCarets = editor.caretModel.allCarets.let { if (options.forward) it else it.reversed() }
            for (caret in allCarets) {
                val startColumn = doFindAndYieldStartColumn(caret, file, options, set)
                doFindAndYieldEndColumn(caret, startColumn, file, options, set)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxCsvColumn>.doFindAndYieldStartColumn(caret: Caret, file: ParadoxCsvFile, options: Options, set: MutableSet<ParadoxCsvColumn>): ParadoxCsvColumn? {
        val offset = if (options.forward) caret.selectionStart else caret.selectionEnd
        val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }
        if (column == null) return null
        if (set.add(column)) yield(column)
        return column
    }

    private suspend fun SequenceScope<ParadoxCsvColumn>.doFindAndYieldEndColumn(caret: Caret, previous: ParadoxCsvColumn?, file: ParadoxCsvFile, options: Options, set: MutableSet<ParadoxCsvColumn>): ParadoxCsvColumn? {
        if (caret.selectionStart == caret.selectionEnd) return null
        val forward = options.forward
        val offset = if (forward) caret.selectionEnd else caret.selectionStart
        val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }?.takeIf { it != previous }
        if (column == null) return null
        val firstRow = previous?.parent
        val lastRow = column.parent
        if (firstRow != null && lastRow != null) {
            if (firstRow == lastRow) {
                val columnsBetween = previous.siblings(forward = forward, withSelf = false).filterIsInstance<ParadoxCsvColumn>().takeWhile { it != column }
                columnsBetween.forEach {
                    if (set.add(it)) yield(it)
                }
            } else {
                val rows = firstRow.siblings(forward = forward, withSelf = false).filterIsInstance<ParadoxCsvRow>().takeWhile { it != lastRow }
                val startIndex = previous.getColumnIndex()
                val endIndex = column.getColumnIndex()
                val columnsBetween = rows.flatMap { row0 ->
                    when {
                        row0 == firstRow -> previous.siblings(forward = forward, withSelf = false)
                            .filterIsInstance<ParadoxCsvColumn>().takeWhile { it.getColumnIndex() <= endIndex }
                        row0 == lastRow -> column.siblings(forward = !forward, withSelf = false)
                            .filterIsInstance<ParadoxCsvColumn>().takeWhile { it.getColumnIndex() >= startIndex }.toList().reversed().asSequence()
                        else -> row0.children(forward = forward)
                            .filterIsInstance<ParadoxCsvColumn>().toList().subList(startIndex, endIndex).asSequence()
                    }
                }
                columnsBetween.forEach {
                    if (set.add(it)) yield(it)
                }
            }
        }
        if (set.add(column)) yield(column)
        return column
    }

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
