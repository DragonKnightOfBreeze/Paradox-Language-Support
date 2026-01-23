@file:Suppress("unused")

package icu.windea.pls.core.ui

import com.intellij.openapi.ui.getOrPutUserData
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.ThreeStateCheckBox
import icu.windea.pls.core.pass
import icu.windea.pls.core.util.createKey
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * 将当前值为布尔类型的映射转化为三态属性。
 */
fun MutableMap<*, Boolean>.toThreeStateProperty() = object : ReadWriteProperty<Any?, ThreeStateCheckBox.State> {
    val map = this@toThreeStateProperty

    override fun getValue(thisRef: Any?, property: KProperty<*>): ThreeStateCheckBox.State {
        return when {
            map.all { (_, v) -> v } -> ThreeStateCheckBox.State.SELECTED
            map.none { (_, v) -> v } -> ThreeStateCheckBox.State.NOT_SELECTED
            else -> ThreeStateCheckBox.State.DONT_CARE
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: ThreeStateCheckBox.State) {
        when (value) {
            ThreeStateCheckBox.State.SELECTED -> map.entries.forEach { it.setValue(true) }
            ThreeStateCheckBox.State.NOT_SELECTED -> map.entries.forEach { it.setValue(false) }
            else -> pass()
        }
    }
}

private val checkBoxListKey = createKey<MutableList<JBCheckBox>>("checkBoxList")

/**
 * 让一个 `JBCheckBox` 与一个 `ThreeStateCheckBox` 联动：
 * - 汇总多个 CheckBox 的选中状态，驱动三态框的状态；
 * - 当三态框切换全选/全不选时，反向更新所有复选框。
 */
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
    if (checkBoxList.isEmpty()) {
        threeStateCheckBox.component.addActionListener {
            when (threeStateCheckBox.component.state) {
                ThreeStateCheckBox.State.SELECTED -> checkBoxList.forEach { it.isSelected = true }
                ThreeStateCheckBox.State.NOT_SELECTED -> checkBoxList.forEach { it.isSelected = false }
                else -> pass()
            }
        }
    }
    checkBoxList.add(this.component)
    return this
}

/** 绑定 `ThreeStateCheckBox` 的状态属性。 */
fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: MutableProperty<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property)
}

/** 绑定 `ThreeStateCheckBox` 的状态属性（基于 Kotlin 属性）。 */
fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: KMutableProperty0<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property.toMutableProperty())
}
