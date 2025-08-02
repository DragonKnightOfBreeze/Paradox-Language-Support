package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.csv.psi.*

object ParadoxCsvManipulator {
    /**
     * 包含选取范围涉及到的所有行。
     */
    fun buildSelectedRowSequence(editor: Editor, file: PsiFile): Sequence<ParadoxCsvRow> {
        return reversibleSequence { operator ->
            val set = mutableSetOf<ParadoxCsvRow>()

            suspend fun SequenceScope<ParadoxCsvRow>.findAndYieldStartRow(caret: Caret): ParadoxCsvRow? {
                if (file !is ParadoxCsvFile) return null
                val offset = if (operator) caret.selectionStart else caret.selectionEnd
                val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }
                if (row == null) return null
                if (set.add(row)) yield(row)
                return row
            }

            suspend fun SequenceScope<ParadoxCsvRow>.findAndYieldEndRow(caret: Caret, previous: ParadoxCsvRow? = null): ParadoxCsvRow? {
                if (file !is ParadoxCsvFile) return null
                if (caret.selectionStart == caret.selectionEnd) return null
                val offset = if (operator) caret.selectionEnd else caret.selectionStart
                val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }?.takeIf { it != previous }
                if (row == null) return null
                val rowsBetween = previous?.siblings(forward = operator, withSelf = false)?.filterIsInstance<ParadoxCsvRow>()?.takeWhile { it != row }
                rowsBetween?.forEach {
                    if (set.add(it)) yield(it)
                }
                if (set.add(row)) yield(row)
                return row
            }

            val allCarets = editor.caretModel.allCarets.letUnless(operator) { it.reversed() }
            for (caret in allCarets) {
                val startRow = findAndYieldStartRow(caret)
                findAndYieldEndRow(caret, previous = startRow)
            }
        }
    }

    /**
     * 包含选取范围涉及到的，索引在选取开始与选取结束各自对应的列的索引区间中的所有列。
     */
    fun buildSelectedColumnSequence(editor: Editor, file: PsiFile): Sequence<ParadoxCsvColumn> {
        return reversibleSequence { operator ->
            val set = mutableSetOf<ParadoxCsvColumn>()

            suspend fun SequenceScope<ParadoxCsvColumn>.findAndYieldStartColumn(caret: Caret): ParadoxCsvColumn? {
                if (file !is ParadoxCsvFile) return null
                val offset = if (operator) caret.selectionStart else caret.selectionEnd
                val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }
                if (column == null) return null
                if (set.add(column)) yield(column)
                return column
            }

            suspend fun SequenceScope<ParadoxCsvColumn>.findAndYieldEndColumn(caret: Caret, previous: ParadoxCsvColumn? = null): ParadoxCsvColumn? {
                if (file !is ParadoxCsvFile) return null
                if (caret.selectionStart == caret.selectionEnd) return null
                val offset = if (operator) caret.selectionEnd else caret.selectionStart
                val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }?.takeIf { it != previous }
                if (column == null) return null
                val firstRow = previous?.parent
                val lastRow = column.parent
                if (firstRow != null && lastRow != null) {
                    if (firstRow == lastRow) {
                        val columnsBetween = previous.siblings(forward = operator, withSelf = false).filterIsInstance<ParadoxCsvColumn>().takeWhile { it != column }
                        columnsBetween.forEach {
                            if (set.add(it)) yield(it)
                        }
                    } else {
                        val rows = firstRow.siblings(forward = operator, withSelf = false).filterIsInstance<ParadoxCsvRow>().takeWhile { it != lastRow }
                        val startIndex = previous.getColumnIndex()
                        val endIndex = column.getColumnIndex()
                        val columnsBetween = rows.flatMap { row0 ->
                            when {
                                row0 == firstRow -> previous.siblings(forward = operator, withSelf = false)
                                    .filterIsInstance<ParadoxCsvColumn>().takeWhile { it.getColumnIndex() <= endIndex }
                                row0 == lastRow -> column.siblings(forward = !operator, withSelf = false)
                                    .filterIsInstance<ParadoxCsvColumn>().takeWhile { it.getColumnIndex() >= startIndex }.toList().reversed().asSequence()
                                else -> row0.children(forward = operator)
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

            val allCarets = editor.caretModel.allCarets.letUnless(operator) { it.reversed() }
            for (caret in allCarets) {
                val startColumn = findAndYieldStartColumn(caret)
                findAndYieldEndColumn(caret, previous = startColumn)
            }
        }
    }

    fun findAllColumnsOfIndex(file: PsiFile, index: Int): Sequence<ParadoxCsvColumn> {
        return file.children().filterIsInstance<ParadoxCsvRowElement>().mapNotNull f@{ rowElement ->
            val column = rowElement.children().filterIsInstance<ParadoxCsvColumn>().drop(index).firstOrNull()
            if (column == null || column.getColumnIndex() != index) return@f null
            column
        }
    }

    fun selectElements(editor: Editor, caretModel: CaretModel, elementList: List<PsiElement>) {
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
