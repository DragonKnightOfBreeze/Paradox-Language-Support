package icu.windea.pls.core.ui

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.*
import com.intellij.openapi.ui.*
import com.intellij.refactoring.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.ui.table.*
import java.awt.*
import javax.swing.table.*
import javax.swing.text.*
import kotlin.reflect.*

fun <T : JTextComponent> Cell<T>.bindText(prop: KMutableProperty0<String?>): Cell<T> {
    return bindText({ prop.get().orEmpty() }, { prop.set(it) })
}

fun Row.pathCompletionShortcutComment() {
    val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION))
    comment(RefactoringBundle.message("path.completion.shortcut", shortcutText))
}

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanelImpl.setFixedColumnWidth
fun JBTable.setFixedColumnWidth(columnIndex: Int, sampleText: String) {
    val table = this
    val column: TableColumn = table.tableHeader.columnModel.getColumn(columnIndex)
    val fontMetrics: FontMetrics = table.getFontMetrics(table.font)
    val width = fontMetrics.stringWidth(" $sampleText ") + scale(4)
    column.preferredWidth = width
    column.minWidth = width
    column.resizable = false
}