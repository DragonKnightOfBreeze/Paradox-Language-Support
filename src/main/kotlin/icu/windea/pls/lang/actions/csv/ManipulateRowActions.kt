@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.psi.PsiFile
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.csv.psi.ParadoxCsvElementFactory
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.getColumnSize
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.util.dataFlow.ParadoxRowSequence
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.lang.util.manipulators.ParadoxCsvManipulator
import kotlinx.coroutines.launch
import java.util.function.Supplier

sealed class InsertRowActionBase(private val above: Boolean) : ManipulateRowActionBase() {
    override fun findElements(e: AnActionEvent, file: PsiFile): ParadoxRowSequence {
        return super.findElements(e, file).options(forward = above)
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence) {
        val anchorRow = elements.firstOrNull() ?: return
        val container = anchorRow.parent ?: return
        val project = file.project
        val header = file.castOrNull<ParadoxCsvFile>()?.header ?: return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                val newRow = ParadoxCsvElementFactory.createEmptyRow(project, header.getColumnSize())
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
    override fun findElements(e: AnActionEvent, file: PsiFile): ParadoxRowSequence {
        return super.findElements(e, file).options(forward = above)
    }

    override fun isEnabled(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Boolean {
        return elements.firstOrNull()?.findOtherRow() != null
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence) {
        //实际上是交换而非移动

        val project = file.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
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
    override fun getTextProvider(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Supplier<String> {
        return Supplier {
            when {
                runReadAction { elements.singleOrNull() } != null -> PlsBundle.message("action.Pls.Manipulation.MoveRowUp.text")
                else -> PlsBundle.message("action.Pls.Manipulation.MoveRowUp.textBatch")
            }
        }
    }
}

class MoveRowDownAction : MoveRowActionBase(above = false) {
    override fun getTextProvider(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Supplier<String> {
        return Supplier {
            when {
                runReadAction { elements.singleOrNull() } != null -> PlsBundle.message("action.Pls.Manipulation.MoveRowDown.text")
                else -> PlsBundle.message("action.Pls.Manipulation.MoveRowDown.textBatch")
            }
        }
    }
}

class SelectRowAction : ManipulateRowActionBase() {
    override fun getTextProvider(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Supplier<String> {
        return Supplier {
            when {
                runReadAction { elements.singleOrNull() } != null -> PlsBundle.message("action.Pls.Manipulation.SelectRow.text")
                else -> PlsBundle.message("action.Pls.Manipulation.SelectRow.textBatch")
            }
        }
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence) {
        val project = file.project
        val editor = e.editor ?: return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val elementList = readAction { elements.toList() }
            if (elementList.isEmpty()) return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                ParadoxCsvManipulator.selectElements(editor, elementList)
            }
        }
    }
}

class RemoveRowAction : ManipulateRowActionBase() {
    override fun getTextProvider(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Supplier<String> {
        return Supplier {
            when {
                runReadAction { elements.singleOrNull() } != null -> PlsBundle.message("action.Pls.Manipulation.RemoveRow.text")
                else -> PlsBundle.message("action.Pls.Manipulation.RemoveRow.textBatch")
            }
        }
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence) {
        val project = file.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
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
