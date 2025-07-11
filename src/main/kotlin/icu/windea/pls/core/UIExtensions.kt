package icu.windea.pls.core

import com.intellij.ide.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.*
import com.intellij.refactoring.*
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.util.*
import java.awt.*
import java.awt.image.*
import java.net.*
import javax.swing.*
import javax.swing.text.*
import kotlin.properties.*
import kotlin.reflect.*

fun String.toIconOrNull(locationClass: Class<*> = PlsFacade::class.java): Icon? {
    //注意这里需要使用反射路径（如，Icons.Test）或者文件URL（而非文件路径）
    return IconLoader.findIcon(this, locationClass)
}

fun URL.toIconOrNull(): Icon? {
    return IconLoader.findIcon(this)
}

operator fun Color.component1() = red
operator fun Color.component2() = green
operator fun Color.component3() = blue
operator fun Color.component4() = alpha

fun Icon.resize(width: Int, height: Int): Icon {
    return IconUtil.toSize(this, width, height)
}

fun Icon.toImage(): Image {
    return IconUtil.toImage(this)
}

fun Icon.toLabel(): JLabel {
    val label = JLabel("", this, SwingConstants.LEADING)
    label.border = JBUI.Borders.empty()
    label.size = label.preferredSize
    label.isOpaque = false
    return label
}

fun Image.toIcon(): Icon {
    return IconUtil.createImageIcon(this)
}

fun JComponent.toImage(width: Int = this.width, height: Int = this.height, type: Int = BufferedImage.TYPE_INT_ARGB_PRE): Image {
    val image = UIUtil.createImage(this, width, height, type)
    UIUtil.useSafely(image.graphics) { this.paint(it) }
    return image
}

fun <T : JComponent> T.withLocation(x: Int, y: Int): T {
    this.setLocation(x, y)
    return this
}

fun <T : JComponent> T.registerClickListener(clickListener: ClickListener, allowDragWhileClicking: Boolean = false) {
    clickListener.installOn(this, allowDragWhileClicking)
}

fun <T : JComponent> T.registerCopyProvider(copyProvider: CopyProvider) {
    DataManager.registerDataProvider(this) { dataId ->
        if (PlatformDataKeys.COPY_PROVIDER.`is`(dataId)) copyProvider else null
    }
}

fun Row.pathCompletionShortcutComment() {
    val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION))
    comment(RefactoringBundle.message("path.completion.shortcut", shortcutText))
}

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

fun <K, V> MutableMap<K, V>.toMutableProperty(key: K, defaultValue: V): MutableProperty<V> {
    return MutableProperty({ getOrPut(key) { defaultValue } }, { put(key, it) })
}

fun <V> PropertyGraph.propertyFrom(property: KMutableProperty0<V>): GraphProperty<V> {
    return lazyProperty { property.get() }.apply { afterChange { property.set(it) } }
}

fun <K, V> PropertyGraph.propertyFrom(map: MutableMap<K, V>, key: K, defaultValue: V): GraphProperty<V> {
    return lazyProperty { map.getOrPut(key) { defaultValue } }.apply { afterChange { map.put(key, it) } }
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

private val checkBoxListKey = createKey<MutableList<JBCheckBox>>("checkBoxList")

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

fun Cell<JBTextField>.bindTextWhenChanged(property: KMutableProperty0<String>): Cell<JBTextField> {
    return applyToComponent {
        whenTextChanged {
            val text = it
            if (text != property.get()) property.set(text)
        }
    }
}

fun Cell<JBTextField>.bindIntTextWhenChanged(property: KMutableProperty0<Int>): Cell<JBTextField> {
    return applyToComponent {
        whenTextChanged {
            val text = it.toIntOrNull() ?: 0
            if (text != property.get()) property.set(text)
        }
    }
}

fun <T : JComponent> Cell<T>.smaller() = customize(UnscaledGaps(3, 0, 3, 0))
