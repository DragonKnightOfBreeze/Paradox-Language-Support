@file:Suppress("unused")

package icu.windea.pls.core

import com.intellij.ide.CopyProvider
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.ui.getOrPutUserData
import com.intellij.openapi.util.IconLoader
import com.intellij.refactoring.RefactoringBundle
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

/** 按反射路径或 URL 查找图标；找不到时返回 null。 */
fun String.toIconOrNull(locationClass: Class<*> = PlsFacade::class.java): Icon? {
    //注意这里需要使用反射路径（如，Icons.Test）或者文件URL（而非文件路径）
    return IconLoader.findIcon(this, locationClass)
}

/** 从 URL 加载图标；找不到时返回 null。 */
fun URL.toIconOrNull(): Icon? {
    return IconLoader.findIcon(this)
}

/** 解构出红色通道（0-255）。 */
operator fun Color.component1() = red
/** 解构出绿色通道（0-255）。 */
operator fun Color.component2() = green
/** 解构出蓝色通道（0-255）。 */
operator fun Color.component3() = blue
/** 解构出透明度通道（0-255）。 */
operator fun Color.component4() = alpha

/** 调整图标尺寸。 */
fun Icon.resize(width: Int, height: Int): Icon {
    return IconUtil.toSize(this, width, height)
}

/** 转为 AWT Image。 */
fun Icon.toImage(): Image {
    return IconUtil.toImage(this)
}

/** 封装为 JLabel（无边框、透明）。 */
fun Icon.toLabel(): JLabel {
    val label = JLabel("", this, SwingConstants.LEADING)
    label.border = JBUI.Borders.empty()
    label.size = label.preferredSize
    label.isOpaque = false
    return label
}

/** 将 Image 包装为 Icon。 */
fun Image.toIcon(): Icon {
    return IconUtil.createImageIcon(this)
}

/** 将组件渲染为图片。 */
fun JComponent.toImage(width: Int = this.width, height: Int = this.height, type: Int = BufferedImage.TYPE_INT_ARGB_PRE): Image {
    val image = UIUtil.createImage(this, width, height, type)
    UIUtil.useSafely(image.graphics) { this.paint(it) }
    return image
}

/** 设置组件位置并返回自身（便于链式调用）。 */
fun <T : JComponent> T.withLocation(x: Int, y: Int): T {
    this.setLocation(x, y)
    return this
}

/** 注册点击监听器，可选允许拖拽时触发。 */
fun <T : JComponent> T.registerClickListener(clickListener: ClickListener, allowDragWhileClicking: Boolean = false) {
    clickListener.installOn(this, allowDragWhileClicking)
}

/** 为组件注册 CopyProvider。 */
fun <T : JComponent> T.registerCopyProvider(copyProvider: CopyProvider) {
    DataManager.registerDataProvider(this) { dataId ->
        if (PlatformDataKeys.COPY_PROVIDER.`is`(dataId)) copyProvider else null
    }
}

/** 在 DSL Row 中显示“路径补全”快捷键提示。 */
fun Row.pathCompletionShortcutComment() {
    val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION))
    comment(RefactoringBundle.message("path.completion.shortcut", shortcutText))
}

/** 将 Map<Boolean> 包装为三态复选框属性。 */
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

/** 将可变 Map 的指定键包装为可变属性。 */
fun <K, V> MutableMap<K, V>.toMutableProperty(key: K, defaultValue: V): MutableProperty<V> {
    return MutableProperty({ getOrPut(key) { defaultValue } }, { put(key, it) })
}

/** 从 Kotlin 属性创建 GraphProperty（双向绑定）。 */
fun <V> PropertyGraph.propertyFrom(property: KMutableProperty0<V>): GraphProperty<V> {
    return lazyProperty { property.get() }.apply { afterChange { property.set(it) } }
}

/** 从 Map 键创建 GraphProperty（双向绑定）。 */
fun <K, V> PropertyGraph.propertyFrom(map: MutableMap<K, V>, key: K, defaultValue: V): GraphProperty<V> {
    return lazyProperty { map.getOrPut(key) { defaultValue } }.apply { afterChange { map.put(key, it) } }
}

/** 绑定三态复选框的状态到属性。 */
fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: MutableProperty<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property)
}

/** 绑定三态复选框的状态到 Kotlin 属性。 */
fun <T : ThreeStateCheckBox> Cell<T>.bindState(property: KMutableProperty0<ThreeStateCheckBox.State>): Cell<T> {
    return bind(ThreeStateCheckBox::getState, ThreeStateCheckBox::setState, property.toMutableProperty())
}

private val checkBoxListKey = createKey<MutableList<JBCheckBox>>("checkBoxList")

/** 将一组二态复选框聚合为一个三态复选框的视图与交互逻辑。 */
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

/** JBTextField 文本变化时，自动同步到字符串属性。 */
fun Cell<JBTextField>.bindTextWhenChanged(property: KMutableProperty0<String>): Cell<JBTextField> {
    return applyToComponent {
        whenTextChanged {
            val text = it
            if (text != property.get()) property.set(text)
        }
    }
}

/** JBTextField 文本变化时，自动同步到整型属性（非数字视为 0）。 */
fun Cell<JBTextField>.bindIntTextWhenChanged(property: KMutableProperty0<Int>): Cell<JBTextField> {
    return applyToComponent {
        whenTextChanged {
            val text = it.toIntOrNull() ?: 0
            if (text != property.get()) property.set(text)
        }
    }
}

/** 使用更紧凑的 Cell 间距。 */
fun <T : JComponent> Cell<T>.smaller() = customize(UnscaledGaps(3, 0, 3, 0))

/** 使用更小的字体。 */
fun <T : JComponent> Cell<T>.smallerFont() = applyToComponent { font = JBUI.Fonts.smallFont() }
