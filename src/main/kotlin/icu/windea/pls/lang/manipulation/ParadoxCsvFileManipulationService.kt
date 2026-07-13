package icu.windea.pls.lang.manipulation

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.WalkingContext
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.forward
import icu.windea.pls.core.findElementAt
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvRow

object ParadoxCsvFileManipulationService {
    /**
     * 构建一个序列，包含当前文件 [file] 中，选取范围涉及到的所有行。
     */
    fun selectedRows(editor: Editor, file: PsiFile): WalkingSequence<ParadoxCsvRow> {
        val context = WalkingContext()
        val delegate = with(context) { buildSelectedRows(file, editor) }
        return WalkingSequence(delegate, context)
    }

    context(context: WalkingContext)
    private fun buildSelectedRows(file: PsiFile, editor: Editor): Sequence<ParadoxCsvRow> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            val set = mutableSetOf<ParadoxCsvRow>()
            val allCarets = editor.caretModel.allCarets.let { if (context.forward) it else it.reversed() }
            for (caret in allCarets) {
                ProgressManager.checkCanceled()
                val startRow = yieldStartRow(file, caret, set)
                yieldEndRow(file, caret, startRow, set)
            }
        }
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxCsvRow>.yieldStartRow(file: ParadoxCsvFile, caret: Caret, set: MutableSet<ParadoxCsvRow>): ParadoxCsvRow? {
        ProgressManager.checkCanceled()
        val offset = if (context.forward) caret.selectionStart else caret.selectionEnd
        val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }
        if (row == null) return null
        if (set.add(row)) yield(row)
        return row
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxCsvRow>.yieldEndRow(file: ParadoxCsvFile, caret: Caret, previous: ParadoxCsvRow?, set: MutableSet<ParadoxCsvRow>): ParadoxCsvRow? {
        ProgressManager.checkCanceled()
        if (caret.selectionStart == caret.selectionEnd) return null
        val forward = context.forward
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
     * 构建一个序列，包含当前文件 [file] 中，选取范围涉及到的，索引在选取开始与选取结束各自对应的列的索引区间中的所有列。
     */
    fun selectedColumns(editor: Editor, file: PsiFile): WalkingSequence<ParadoxCsvColumn> {
        val context = WalkingContext()
        val delegate = with(context) { buildSelectedColumns(file, editor) }
        return WalkingSequence(delegate, context)
    }

    context(context: WalkingContext)
    private fun buildSelectedColumns(file: PsiFile, editor: Editor): Sequence<ParadoxCsvColumn> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            val set = mutableSetOf<ParadoxCsvColumn>()
            val allCarets = editor.caretModel.allCarets.let { if (context.forward) it else it.reversed() }
            for (caret in allCarets) {
                ProgressManager.checkCanceled()
                val startColumn = yieldStartColumn(file, caret, set)
                yieldEndColumn(file, caret, startColumn, set)
            }
        }
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxCsvColumn>.yieldStartColumn(file: ParadoxCsvFile, caret: Caret, set: MutableSet<ParadoxCsvColumn>): ParadoxCsvColumn? {
        ProgressManager.checkCanceled()
        val offset = if (context.forward) caret.selectionStart else caret.selectionEnd
        val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }
        if (column == null) return null
        if (set.add(column)) yield(column)
        return column
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxCsvColumn>.yieldEndColumn(file: ParadoxCsvFile, caret: Caret, previous: ParadoxCsvColumn?, set: MutableSet<ParadoxCsvColumn>): ParadoxCsvColumn? {
        ProgressManager.checkCanceled()
        if (caret.selectionStart == caret.selectionEnd) return null
        val forward = context.forward
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
                val startIndex = ParadoxCsvPsiService.getColumnIndex(previous)
                val endIndex = ParadoxCsvPsiService.getColumnIndex(column)
                val columnsBetween = rows.flatMap { row0 ->
                    when (row0) {
                        firstRow -> previous.siblings(forward = forward, withSelf = false)
                            .filterIsInstance<ParadoxCsvColumn>().takeWhile { ParadoxCsvPsiService.getColumnIndex(it) <= endIndex }
                        lastRow -> column.siblings(forward = !forward, withSelf = false)
                            .filterIsInstance<ParadoxCsvColumn>().takeWhile { ParadoxCsvPsiService.getColumnIndex(it) >= startIndex }.toList().reversed().asSequence()
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

    /**
     * 构建一个序列，包含当前文件 [file] 中，列索引为 [index] 的所有列。
     */
    fun columnsOfIndex(file: PsiFile, index: Int, includeHeaderColumn: Boolean = true): WalkingSequence<ParadoxCsvColumn> {
        val context = WalkingContext()
        val delegate = with(context) { buildColumnsOfIndex(file, index, includeHeaderColumn) }
        return WalkingSequence(delegate, context)
    }

    context(context: WalkingContext)
    private fun buildColumnsOfIndex(file: PsiFile, index: Int, includeHeaderColumn: Boolean = true): Sequence<ParadoxCsvColumn> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            for (columnContainer in file.children(context.forward)) {
                if (columnContainer !is ParadoxCsvColumnContainer) continue
                if (!includeHeaderColumn && columnContainer is ParadoxCsvHeader) continue
                val column = columnContainer.children().filterIsInstance<ParadoxCsvColumn>().drop(index).firstOrNull()
                if (column == null || ParadoxCsvPsiService.getColumnIndex(column) != index) continue
                yield(column)
            }
        }
    }
}
