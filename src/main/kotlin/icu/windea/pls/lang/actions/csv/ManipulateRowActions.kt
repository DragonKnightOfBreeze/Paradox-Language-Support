@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.actions.*
import kotlinx.coroutines.*

sealed class MoveRowActionBase(private val above: Boolean) : ManipulateRowActionBase() {
    override fun findElements(editor: Editor, file: ParadoxCsvFile): Sequence<ParadoxCsvRow> {
        return super.findElements(editor, file).letUnless(above) { it.reversed() }
    }

    override fun isEnabled(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>): Boolean {
        return elements.firstOrNull()?.findOtherRow() != null
    }

    override fun doInvoke(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for (row in elements) {
                    val otherRow = row.findOtherRow() ?: continue
                    val copied = row.copy()
                    row.replace(otherRow)
                    otherRow.replace(copied)
                }
            }
        }
    }

    private fun ParadoxCsvRow.findOtherRow(): ParadoxCsvRow? {
        return this.siblings(forward = !above, withSelf = false).findIsInstance<ParadoxCsvRow>()
    }
}

class MoveRowUpAction : MoveRowActionBase(above = true)

class MoveRowDownAction : MoveRowActionBase(above = false)

sealed class InsertRowActionBase(private val above: Boolean) : ManipulateRowActionBase() {
    override fun findElements(editor: Editor, file: ParadoxCsvFile): Sequence<ParadoxCsvRow> {
        return super.findElements(editor, file).letUnless(above) { it.reversed() }
    }

    override fun doInvoke(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>) {
        val anchorRow = elements.firstOrNull() ?: return
        val container = anchorRow.parent ?: return
        val header = file.castOrNull<ParadoxCsvFile>()?.header ?: return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                val newRow = ParadoxCsvElementFactory.createEmptyRow(project, header.getColumnSize())
                if(above) {
                    container.addBefore(newRow, anchorRow)
                } else {
                    container.addAfter(newRow, anchorRow)
                }
            }
        }
    }
}

class InsertRowAboveAction : InsertRowActionBase(above = true)

class InsertRowBelowAction : InsertRowActionBase(above = false)

class SelectRowAction : ManipulateRowActionBase() {
    override fun doInvoke(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>) {
        val editor = e.editor ?: return
        val caretModel = editor.caretModel
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                caretModel.removeSecondaryCarets()
                var usePrimary = true
                for (row in elements) {
                    val range = row.textRange
                    if (usePrimary) {
                        caretModel.primaryCaret.setSelection(range.startOffset, range.endOffset)
                        usePrimary = false
                    } else {
                        val caret = caretModel.addCaret(editor.offsetToVisualPosition(range.startOffset), false)
                        caret?.setSelection(range.startOffset, range.endOffset)
                    }
                }
            }
        }
    }
}

class RemoveRowAction : ManipulateRowActionBase() {
    override fun doInvoke(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for (row in elements) {
                    row.delete()
                }
            }
        }
    }
}
