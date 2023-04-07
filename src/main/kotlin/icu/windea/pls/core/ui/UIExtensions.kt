package icu.windea.pls.core.ui

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.*
import com.intellij.refactoring.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.ui.table.*
import com.intellij.util.ui.ThreeStateCheckBox
import icu.windea.pls.core.*
import java.awt.*
import javax.swing.table.*
import javax.swing.text.*
import kotlin.properties.*
import kotlin.reflect.*

fun MutableMap<*, Boolean>.toThreeStateProperty() = object : ReadWriteProperty<Any, ThreeStateCheckBox.State> {
    val map = this@toThreeStateProperty
    
    override fun getValue(thisRef: Any, property: KProperty<*>): ThreeStateCheckBox.State {
        return when {
            map.all { (_, v) -> v } -> ThreeStateCheckBox.State.SELECTED
            map.none { (_, v) -> v } -> ThreeStateCheckBox.State.NOT_SELECTED
            else -> ThreeStateCheckBox.State.DONT_CARE
        }
    }
    
    override fun setValue(thisRef: Any, property: KProperty<*>, value: ThreeStateCheckBox.State) {
        when(value) {
            ThreeStateCheckBox.State.SELECTED -> map.entries.forEach { it.setValue(true) }
            ThreeStateCheckBox.State.NOT_SELECTED -> map.entries.forEach { it.setValue(false) }
            else -> pass()
        }
    }
}

fun <K, V> MutableMap<K, V>.toMutableProperty(key: K, defaultValue: V): MutableProperty<V> {
    return MutableProperty({
        getOrPut(key) { defaultValue }
    }, {
        put(key, it)
    })
}


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
    column.maxWidth = width
    column.resizable = false
}