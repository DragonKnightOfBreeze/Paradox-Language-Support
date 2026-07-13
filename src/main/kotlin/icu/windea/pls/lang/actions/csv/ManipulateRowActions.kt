@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.psi.PsiFile
import com.intellij.psi.util.siblings
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.context
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.forward
import icu.windea.pls.core.editor
import icu.windea.pls.core.editor.EditorService
import icu.windea.pls.csv.psi.ParadoxCsvElementFactory
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvRow
import kotlinx.coroutines.launch

sealed class InsertRowActionBase(private val above: Boolean) : ManipulateRowActionBase() {
    override fun findElements(e: AnActionEvent, file: PsiFile): WalkingSequence<ParadoxCsvRow> {
        return super.findElements(e, file).context { forward(above) }
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val anchorRow = elements.firstOrNull() ?: return
        val container = anchorRow.parent ?: return
        val project = file.project
        val header = file.castOrNull<ParadoxCsvFile>()?.header ?: return
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                val newRow = ParadoxCsvElementFactory.createEmptyRow(project, ParadoxCsvPsiService.getColumnSize(header))
                if (above) {
                    container.addRangeBefore(newRow, newRow.nextSibling, anchorRow)
                } else {
                    container.addRangeAfter(newRow.prevSibling, newRow, anchorRow)
                }
            }
        }
    }
}

class InsertRowAboveAction : InsertRowActionBase(above = true)

class InsertRowBelowAction : InsertRowActionBase(above = false)

sealed class MoveRowActionBase(private val above: Boolean) : ManipulateRowActionBase() {
    override fun findElements(e: AnActionEvent, file: PsiFile): WalkingSequence<ParadoxCsvRow> {
        return super.findElements(e, file).context { forward(above) }
    }

    override fun isEnabled(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>): Boolean {
        return elements.firstOrNull()?.findOtherRow() != null
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        // 实际上是交换而非移动

        val project = file.project
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val elementList = readAction { elements.toList() }
            if (elementList.isEmpty()) return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for (row in elementList) {
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

class MoveRowUpAction : MoveRowActionBase(above = true) {
    override fun doUpdate(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val text = when {
            elements.singleOrNull() != null -> ChronicleBundle.message("action.Pls.Manipulation.MoveRowUp.text")
            else -> ChronicleBundle.message("action.Pls.Manipulation.MoveRowUp.textBatch")
        }
        e.presentation.text = text
    }
}

class MoveRowDownAction : MoveRowActionBase(above = false) {
    override fun doUpdate(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val text = when {
            elements.singleOrNull() != null -> ChronicleBundle.message("action.Pls.Manipulation.MoveRowDown.text")
            else -> ChronicleBundle.message("action.Pls.Manipulation.MoveRowDown.textBatch")
        }
        e.presentation.text = text
    }
}

class SelectRowAction : ManipulateRowActionBase() {
    override fun doUpdate(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val text = when {
            elements.singleOrNull() != null -> ChronicleBundle.message("action.Pls.Manipulation.SelectRow.text")
            else -> ChronicleBundle.message("action.Pls.Manipulation.SelectRow.textBatch")
        }
        e.presentation.text = text
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val project = file.project
        val editor = e.editor ?: return
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val elementList = readAction { elements.toList() }
            if (elementList.isEmpty()) return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                EditorService.selectElements(editor, elementList)
            }
        }
    }
}

class RemoveRowAction : ManipulateRowActionBase() {
    override fun doUpdate(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val text = when {
            elements.singleOrNull() != null -> ChronicleBundle.message("action.Pls.Manipulation.RemoveRow.text")
            else -> ChronicleBundle.message("action.Pls.Manipulation.RemoveRow.textBatch")
        }
        e.presentation.text = text
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {
        val project = file.project
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val elementList = readAction { elements.toList() }
            if (elementList.isEmpty()) return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for (row in elementList) {
                    row.delete()
                }
            }
        }
    }
}
