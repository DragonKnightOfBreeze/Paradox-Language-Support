package icu.windea.pls.core.ui

import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

/**
 * 将 `Map<K,V>` 的某个键值映射为可写属性（读写操作会同步至 Map）。
 */
fun <K, V> MutableMap<K, V>.toMutableProperty(key: K, defaultValue: V): MutableProperty<V> {
    return MutableProperty({ getOrPut(key) { defaultValue } }, { put(key, it) })
}

/** 应用更小的外边距。 */
fun <T : JComponent> Cell<T>.smaller() = customize(UnscaledGaps(3, 0, 3, 0))

/** 应用更小的字体。 */
fun <T : JComponent> Cell<T>.smallerFont() = applyToComponent { font = JBUI.Fonts.smallFont() }
