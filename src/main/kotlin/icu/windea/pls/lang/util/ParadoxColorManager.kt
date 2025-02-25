package icu.windea.pls.lang.util

import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.script.psi.*
import java.awt.*

@Suppress("UseJBColor")
object ParadoxColorManager {
    // ## color_type = hex
    // format:
    // 0xffffffff (ignore case, 8 or 10 length)

    // ## color_type = rgb
    // format:
    // rgb { $r $g $b }
    // rgb { $r $g $b $a }
    // r,g,b,a - int[0..255] or float[0.0..1.0]

    // ## color_type = hsv
    // format:
    // hsv { $h $s $v }
    // h,s,v - float[0.0..1.0]

    // ## color_type = hsv360 (for vic3 and others)
    // format:
    // hsv360 { $h $s $v }
    // h - int[0..360]
    // s,v - int[0..100]

    fun getColor(hex: String): Color {
        return ColorUtil.fromHex(hex)
    }

    fun getColor(colorType: String, colorArgs: List<String>): Color? {
        return when (colorType) {
            "rgb" -> getRgbColor(colorArgs)
            "hsv" -> getHsvColor(colorArgs)
            "hsv360" -> getHsv360Color(colorArgs)
            else -> null
        }
    }

    fun getRgbColor(colorArgs: List<String>): Color? {
        if (colorArgs.size != 3 && colorArgs.size != 4) return null
        val useFloat = colorArgs.all { it.toFloat() in 0f..1f } && colorArgs.any { it.contains('.') }
        if (useFloat) {
            val r = colorArgs.get(0).toFloat().let { it * 255 }.toInt().coerceIn(0..255)
            val g = colorArgs.get(1).toFloat().let { it * 255 }.toInt().coerceIn(0..255)
            val b = colorArgs.get(2).toFloat().let { it * 255 }.toInt().coerceIn(0..255)
            val a = colorArgs.getOrNull(3)?.toFloat()?.let { it * 255 }?.toInt() ?: 255 // a - < 0 or > 255 is allowed
            return Color(r, g, b, a) //r,g,b,a - int[0..255]
        } else {
            val r = colorArgs.get(0).toInt().takeIf { it in 0..255 } ?: return null
            val g = colorArgs.get(1).toInt().takeIf { it in 0..255 } ?: return null
            val b = colorArgs.get(2).toInt().takeIf { it in 0..255 } ?: return null
            val a = colorArgs.getOrNull(3)?.toInt() ?: 255 // a - < 0 or > 255 is allowed
            return Color(r, g, b, a) //r,g,b,a - int[0..255]
        }
    }

    fun getHsvColor(colorArgs: List<String>): Color? {
        if (colorArgs.size != 3) return null
        val h = colorArgs.get(0).toFloat().coerceIn(0f..1f)
        val s = colorArgs.get(1).toFloat().coerceIn(0f..1f)
        val v = colorArgs.get(2).toFloat().coerceIn(0f..1f)
        val (r, g, b) = Color.getHSBColor(h, s, v) //h,s,v - float[0.0..1.0]
        return Color(r, g, b)
    }

    fun getHsv360Color(colorArgs: List<String>): Color? {
        if (colorArgs.size != 3) return null
        val h = colorArgs.get(0).toInt().let { it / 360.0f }.coerceIn(0f..1f)
        val s = colorArgs.get(1).toInt().let { it / 100.0f }.coerceIn(0f..1f)
        val v = colorArgs.get(2).toInt().let { it / 100.0f }.coerceIn(0f..1f)
        val (r, g, b) = Color.getHSBColor(h, s, v) //h,s,v - float[0.0..1.0]
        return Color(r, g, b)
    }

    fun getNewColorArgs(colorType: String, colorArgs: List<String>, newColor: Color): List<String>? {
        //保留3位小数
        val precision = -3
        return when (colorType) {
            "rgb" -> {
                if (colorArgs.size != 3 && colorArgs.size != 4) return null
                val useFloat = colorArgs.all { it.toFloat() in 0f..1f } && colorArgs.any { it.contains('.') }
                val addAlpha = colorArgs.size == 4
                val (r, g, b, a) = newColor
                listOf(r, g, b, a).let { if (addAlpha) it else it.take(3) }.map { if (useFloat) (it / 255.0).format(precision) else it.toString() }
            }
            "hsv" -> {
                if (colorArgs.size != 3) return null
                val (r, g, b) = newColor
                val (h, s, v) = Color.RGBtoHSB(r, g, b, null)
                listOf(h,s,v).map { it.format(precision) }
            }
            "hsv360" -> {
                if (colorArgs.size != 3) return null
                val (r, g, b) = newColor
                val (h, s, v) = Color.RGBtoHSB(r, g, b, null)
                listOf(h,s,v).map { (it * 360).toInt().toString() }
            }
            else -> null
        }
    }

    /**
     * 得到当前PSI元素[element]对应的颜色类型。
     *
     * 从匹配的规则的`color_type`选项的值获取。
     *
     * 可选值：`hex` `rgb` `hsv` `hsv360`。
     * 其中`hex`仅适用于[element]是[ParadoxScriptString]的场合，
     * 其他可选值适用于[element]是[ParadoxScriptBlock]或[ParadoxScriptColor]的场合。
     */
    fun getColorType(element: PsiElement): String? {
        val configToGetOption = ParadoxExpressionManager.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
            .firstOrNull()
        if (configToGetOption == null) return null
        return getColorType(configToGetOption)
    }

    private fun getColorType(configToGetOption: CwtMemberConfig<*>): String? {
        return configToGetOption.findOption { it.key == "color_type" }?.stringValue
    }
}
