@file:Suppress("unused")

package icu.windea.pls.core.ui

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ClickListener
import com.intellij.ui.DoubleClickListener
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import icu.windea.pls.ChronicleFacade
import java.awt.Color
import java.awt.Image
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.net.URL
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants

operator fun Color.component1() = red
operator fun Color.component2() = green
operator fun Color.component3() = blue
operator fun Color.component4() = alpha

/** 调整图标尺寸为 [width]×[height]。 */
fun Icon.resize(width: Int, height: Int): Icon {
    return IconUtil.toSize(this, width, height)
}

/** 将图标转换为 `Image`。 */
fun Icon.toImage(): Image {
    return IconUtil.toImage(this)
}

/**
 * 尝试从反射路径或资源 URL 加载图标。
 *
 * 注意：应传入反射路径（如 `"/icons/xxx.svg"` 或 `Icons.Test`）或 URL（而非文件系统路径）。
 */
fun String.toIconOrNull(locationClass: Class<*> = ChronicleFacade::class.java): Icon? {
    // 注意这里需要使用反射路径（如，Icons.Test）或者文件URL（而非文件路径）
    return IconLoader.findIcon(this, locationClass)
}

/** 从 URL 加载图标。 */
fun URL.toIconOrNull(): Icon? {
    return IconLoader.findIcon(this)
}

/** 将图标包装为纯展示用的 `JLabel`（无边框、透明背景）。 */
fun Icon.toLabel(): JLabel {
    val label = JLabel("", this, SwingConstants.LEADING)
    label.border = JBUI.Borders.empty()
    label.size = label.preferredSize
    label.isOpaque = false
    return label
}

/** 将 `Image` 转为 `Icon`。 */
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

/** 设置组件坐标并返回自身（便于链式调用）。 */
fun <T : JComponent> T.withLocation(x: Int, y: Int): T {
    this.setLocation(x, y)
    return this
}

inline fun <T : JComponent> T.registerSingleClickListener(allowDragWhileClicking: Boolean = false, crossinline action: (event: MouseEvent) -> Unit) {
    val listener = object : ClickListener() {
        override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
            action(event)
            return true
        }
    }
    listener.installOn(this, allowDragWhileClicking)
}

inline fun <T : JComponent> T.registerDoubleClickListener(allowDragWhileClicking: Boolean = false, crossinline action: (event: MouseEvent) -> Unit) {
    val listener = object : DoubleClickListener() {
        override fun onDoubleClick(event: MouseEvent): Boolean {
            action(event)
            return true
        }
    }
    listener.installOn(this, allowDragWhileClicking)
}
