package icu.windea.pls.core

import com.intellij.ui.*
import com.intellij.util.*
import java.awt.*
import javax.swing.*

fun Icon.resize(width: Int, height: Int): Icon {
    return IconUtil.toSize(this, width, height)
}

fun Color.toHex() = ColorUtil.toHex(this)

operator fun Color.component1() = red
operator fun Color.component2() = green
operator fun Color.component3() = blue
operator fun Color.component4() = alpha