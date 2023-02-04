package icu.windea.pls.core

import com.intellij.ui.*
import com.intellij.util.*
import org.apache.commons.imaging.color.*
import java.awt.*
import javax.swing.*

fun Icon.resize(width:Int, height:Int): Icon {
	return IconUtil.toSize(this, width, height)
}

fun Color.toHex() = ColorUtil.toHex(this)

fun ColorHsl.toColor() = Color(ColorConversions.convertHSLtoRGB(this))

//fun Color.toColorHsl() = ColorConversions.convertRGBtoHSL(this.rgb)

fun ColorHsv.toColor() = Color(ColorConversions.convertHSVtoRGB(this))

//fun Color.toColorHsv() = ColorConversions.convertRGBtoHSV(this.rgb)