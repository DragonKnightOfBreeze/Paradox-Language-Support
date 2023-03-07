package icu.windea.pls.core

import com.intellij.ui.*
import com.intellij.util.*
import com.intellij.util.ui.*
import java.awt.*
import java.awt.image.*
import javax.swing.*

fun Icon.resize(width: Int, height: Int): Icon {
    return IconUtil.toSize(this, width, height)
}

fun Image.toIcon(): Icon {
    return IconUtil.createImageIcon(this)
}

fun Icon.toImage(): Image {
    return IconUtil.toImage(this)
}

fun Icon.toLabel(): JLabel {
    return JLabel("", this, SwingConstants.LEADING)
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

fun Color.toHex(withAlpha: Boolean = true) = ColorUtil.toHex(this, withAlpha)

operator fun Color.component1() = red
operator fun Color.component2() = green
operator fun Color.component3() = blue
operator fun Color.component4() = alpha