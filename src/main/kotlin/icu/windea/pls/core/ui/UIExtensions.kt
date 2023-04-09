package icu.windea.pls.core.ui

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.refactoring.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.ui.table.*
import com.intellij.util.ui.*
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
    return MutableProperty({ getOrPut(key) { defaultValue } }, { put(key, it) })
}

fun <V> PropertyGraph.propertyFrom(property: KMutableProperty0<V>): GraphProperty<V> {
    return lazyProperty { property.get() }.apply { afterChange { property.set(it) } }
}

fun <K, V> PropertyGraph.propertyFrom(map: MutableMap<K, V>, key: K, defaultValue: V): GraphProperty<V> {
    return lazyProperty { map.getOrPut(key) { defaultValue } }.apply { afterChange { map.put(key, it) } }
}

fun <T1, T2> MutableProperty<T1>.bindWith(other: MutableProperty<T2>): MutableProperty<T1> {
    
    return this
}


fun <T : JTextComponent> Cell<T>.bindText(prop: KMutableProperty0<String?>): Cell<T> {
    return bindText({ prop.get().orEmpty() }, { prop.set(it) })
}

fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: MutableProperty<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property)
}

fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: KMutableProperty0<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property.toMutableProperty())
}

val checkBoxListKey = Key.create<MutableList<JBCheckBox>>("checkBoxList")

fun <T : Cell<JBCheckBox>> T.threeStateCheckBox(threeStateCheckBox: Cell<ThreeStateCheckBox>): T {
    val checkBoxList = threeStateCheckBox.component.getOrPutUserData(checkBoxListKey) { mutableListOf() }
    threeStateCheckBox.component.state = when {
        checkBoxList.all { it.isSelected } -> ThreeStateCheckBox.State.SELECTED
        checkBoxList.none { it.isSelected } -> ThreeStateCheckBox.State.NOT_SELECTED
        else -> ThreeStateCheckBox.State.DONT_CARE
    }
    this.component.addActionListener {
        threeStateCheckBox.component.state = when {
            checkBoxList.all { it.isSelected } -> ThreeStateCheckBox.State.SELECTED
            checkBoxList.none { it.isSelected } -> ThreeStateCheckBox.State.NOT_SELECTED
            else -> ThreeStateCheckBox.State.DONT_CARE
        }
    }
    if(checkBoxList.isEmpty()) {
        threeStateCheckBox.component.addActionListener {
            when(threeStateCheckBox.component.state) {
                ThreeStateCheckBox.State.SELECTED -> checkBoxList.forEach { it.isSelected = true }
                ThreeStateCheckBox.State.NOT_SELECTED -> checkBoxList.forEach { it.isSelected = false }
                else -> pass()
            }
        }
    }
    checkBoxList.add(this.component)
    return this
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