@file:Suppress("unused")

package icu.windea.pls.core

import com.intellij.ide.CopyProvider
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.ui.getOrPutUserData
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ClickListener
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ThreeStateCheckBox
import com.intellij.util.ui.UIUtil
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.createKey
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.net.URL
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * 尝试从反射路径或资源 URL 加载图标。
 *
 * 注意：应传入反射路径（如 `"/icons/xxx.svg"` 或 `Icons.Test`）或 URL（而非文件系统路径）。
 */
fun String.toIconOrNull(locationClass: Class<*> = PlsFacade::class.java): Icon? {
    //注意这里需要使用反射路径（如，Icons.Test）或者文件URL（而非文件路径）
    return IconLoader.findIcon(this, locationClass)
}

/** 从 URL 加载图标。*/
fun URL.toIconOrNull(): Icon? {
    return IconLoader.findIcon(this)
}

operator fun Color.component1() = red
operator fun Color.component2() = green
operator fun Color.component3() = blue
operator fun Color.component4() = alpha

/** 调整图标尺寸为 [width]×[height]。*/
fun Icon.resize(width: Int, height: Int): Icon {
    return IconUtil.toSize(this, width, height)
}

/** 将图标转换为 `Image`。*/
fun Icon.toImage(): Image {
    return IconUtil.toImage(this)
}

/** 将图标包装为纯展示用的 `JLabel`（无边框、透明背景）。*/
fun Icon.toLabel(): JLabel {
    val label = JLabel("", this, SwingConstants.LEADING)
    label.border = JBUI.Borders.empty()
    label.size = label.preferredSize
    label.isOpaque = false
    return label
}

/** 将 `Image` 转为 `Icon`。*/
fun Image.toIcon(): Icon {
    return IconUtil.createImageIcon(this)
}

/**
 * 将组件渲染为图片（可指定输出宽高与图片类型）。
 */
fun JComponent.toImage(width: Int = this.width, height: Int = this.height, type: Int = BufferedImage.TYPE_INT_ARGB_PRE): Image {
    val image = UIUtil.createImage(this, width, height, type)
    UIUtil.useSafely(image.graphics) { this.paint(it) }
    return image
}

/** 设置组件坐标并返回自身（便于链式调用）。*/
fun <T : JComponent> T.withLocation(x: Int, y: Int): T {
    this.setLocation(x, y)
    return this
}

/** 为组件注册点击监听器。*/
fun <T : JComponent> T.registerClickListener(clickListener: ClickListener, allowDragWhileClicking: Boolean = false) {
    clickListener.installOn(this, allowDragWhileClicking)
}

/** 为组件注册剪贴板复制提供者。*/
fun <T : JComponent> T.registerCopyProvider(copyProvider: CopyProvider) {
    DataManager.registerDataProvider(this) { dataId ->
        if (PlatformDataKeys.COPY_PROVIDER.`is`(dataId)) copyProvider else null
    }
}

// fun KMutableProperty0<Boolean>.toAtomicProperty(): AtomicBooleanProperty {
//     return AtomicBooleanProperty(get()).apply { afterChange { set(it) } }
// }
//
// fun <T> KMutableProperty0<T>.toAtomicProperty(): AtomicProperty<T> {
//     return AtomicProperty(get()).apply { afterChange { set(it) } }
// }
//
// fun <T : Any> KMutableProperty0<T?>.toAtomicProperty(defaultValue: T): AtomicProperty<T> {
//     return AtomicProperty(get() ?: defaultValue).apply { afterChange { set(it) } }
// }

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

/**
 * 将 `Map<K,V>` 的某个键值映射为可写属性（读写操作会同步至 Map）。
 */
fun <K, V> MutableMap<K, V>.toMutableProperty(key: K, defaultValue: V): MutableProperty<V> {
    return MutableProperty({ getOrPut(key) { defaultValue } }, { put(key, it) })
}

/**
 * 基于现有 `KMutableProperty0` 生成 GraphProperty，并在值变化时回写。
 */
fun <V> PropertyGraph.propertyFrom(property: KMutableProperty0<V>): GraphProperty<V> {
    return lazyProperty { property.get() }.apply { afterChange { property.set(it) } }
}

/**
 * 基于 `Map` 的键值生成 GraphProperty，并在值变化时同步到 Map。
 */
fun <K, V> PropertyGraph.propertyFrom(map: MutableMap<K, V>, key: K, defaultValue: V): GraphProperty<V> {
    return lazyProperty { map.getOrPut(key) { defaultValue } }.apply { afterChange { map.put(key, it) } }
}

/** 绑定 `ThreeStateCheckBox` 的状态属性。*/
fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: MutableProperty<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property)
}

/** 绑定 `ThreeStateCheckBox` 的状态属性（基于 Kotlin 属性）。*/
fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: KMutableProperty0<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property.toMutableProperty())
}

/** 存放多选框联动的组件列表的 Key。*/
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

/** 当文本变化时双向绑定到 `property`。*/
fun Cell<JBTextField>.bindTextWhenChanged(property: KMutableProperty0<String>): Cell<JBTextField> {
    return applyToComponent {
        whenTextChanged {
            val text = it
            if (text != property.get()) property.set(text)
        }
    }
}

/** 当文本变化时尝试解析为 Int 并写回 `property`。*/
fun Cell<JBTextField>.bindIntTextWhenChanged(property: KMutableProperty0<Int>): Cell<JBTextField> {
    return applyToComponent {
        whenTextChanged {
            val text = it.toIntOrNull() ?: 0
            if (text != property.get()) property.set(text)
        }
    }
}

/** 应用更小的外边距。*/
fun <T : JComponent> Cell<T>.smaller() = customize(UnscaledGaps(3, 0, 3, 0))

/** 应用更小的字体。*/
fun <T : JComponent> Cell<T>.smallerFont() = applyToComponent { font = JBUI.Fonts.smallFont() }
