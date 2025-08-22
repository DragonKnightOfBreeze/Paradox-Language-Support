@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*

class SelectColumnCellAction : ManipulateColumnActionBase() {
    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxColumnSequence) {
        //目前不支持批量处理

        val project = file.project
        val editor = e.editor ?: return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val column = readAction { elements.firstOrNull() } ?: return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                ParadoxCsvManipulator.selectElement(editor, column)
            }
        }
    }
}

sealed class InsertColumnActionBase(private val left: Boolean) : ManipulateColumnActionBase() {
    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxColumnSequence) {
        val project = file.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val index = readAction { elements.firstOrNull()?.getColumnIndex() } ?: return@launch
            val columnList = readAction {
                ParadoxCsvManipulator.findAllColumnsOfIndex(file, index).toList()
            }
            if (columnList.isEmpty()) return@launch
            val anchorList = readAction {
                columnList.map { column ->
                    val next = column.siblings(forward = !left, withSelf = false).dropWhile { it.elementType == WHITE_SPACE }.firstOrNull()
                    next ?: column
                }
            }
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for (anchor in anchorList) {
                    val container = anchor.parent ?: continue
                    val newSeparator = ParadoxCsvElementFactory.createSeparator(project)
                    if (left) {
                        container.addBefore(newSeparator, anchor)
                    } else {
                        if (anchor.elementType != SEPARATOR) {
                            val newSeparator1 = ParadoxCsvElementFactory.createSeparator(project)
                            container.addAfter(newSeparator1, anchor)
                        }
                        container.addAfter(newSeparator, anchor)
                    }
                }
            }
        }
    }
}

class InsertColumnLeftAction : InsertColumnActionBase(left = true)

class InsertColumnRightAction : InsertColumnActionBase(left = false)

sealed class MoveColumnActionBase(private val left: Boolean) : ManipulateColumnActionBase() {
    override fun isEnabled(e: AnActionEvent, file: PsiFile, elements: ParadoxColumnSequence): Boolean {
        return elements.firstOrNull()?.findOtherColumn() != null
    }

    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxColumnSequence) {
        //目前不支持批量处理
        //实际上是交换而非移动

        val project = file.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val index = readAction { elements.firstOrNull()?.getColumnIndex() } ?: return@launch
            val columnList = readAction {
                ParadoxCsvManipulator.findAllColumnsOfIndex(file, index).toList()
            }
            if (columnList.isEmpty()) return@launch
            val columnAndOtherColumnList = readAction {
                columnList.mapNotNull f@{ column ->
                    val otherColumn = column.findOtherColumn() ?: return@f null
                    tupleOf(column, otherColumn)
                }
            }
            if (columnAndOtherColumnList.isEmpty()) return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for ((column, otherColumn) in columnAndOtherColumnList) {
                    val copied = column.copy()
                    column.replace(otherColumn)
                    otherColumn.replace(copied)
                }
            }
        }
    }

    private fun ParadoxCsvColumn.findOtherColumn(): ParadoxCsvColumn? {
        return this.siblings(forward = !left, withSelf = false).findIsInstance<ParadoxCsvColumn>()
    }
}

class MoveColumnLeftAction : MoveColumnActionBase(left = true)

class MoveColumnRightAction : MoveColumnActionBase(left = false)

class SelectColumnAction : ManipulateColumnActionBase() {
    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxColumnSequence) {
        //目前不支持批量处理

        val project = file.project
        val editor = e.editor ?: return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val index = readAction { elements.firstOrNull()?.getColumnIndex() } ?: return@launch
            val columnList = readAction {
                ParadoxCsvManipulator.findAllColumnsOfIndex(file, index).toList()
            }
            if (columnList.isEmpty()) return@launch
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                ParadoxCsvManipulator.selectElements(editor, columnList)
            }
        }
    }
}

class RemoveColumnAction : ManipulateColumnActionBase() {
    override fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxColumnSequence) {
        //目前不支持批量处理

        val project = file.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val index = readAction { elements.firstOrNull()?.getColumnIndex() } ?: return@launch
            val columnList = readAction {
                ParadoxCsvManipulator.findAllColumnsOfIndex(file, index).toList()
            }
            if (columnList.isEmpty()) return@launch
            val firstAndLastList = readAction {
                columnList.map { column ->
                    val next = column.siblings(forward = true, withSelf = false).takeWhile { it.elementType == WHITE_SPACE || it.elementType == SEPARATOR }.lastOrNull()
                    val prev = column.siblings(forward = false, withSelf = false).takeWhile { it.elementType == WHITE_SPACE }.lastOrNull()
                    tupleOf(prev ?: column, next ?: column)
                }
            }
            val commandName = e.presentation.text
            writeCommandAction(project, commandName) {
                for ((first, last) in firstAndLastList) {
                    first.parent?.deleteChildRange(first, last)
                }
            }
        }
    }
}
