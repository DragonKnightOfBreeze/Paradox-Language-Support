package icu.windea.pls.core.ui

import com.intellij.ui.ColorUtil
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.allFast
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.component1
import icu.windea.pls.core.component2
import icu.windea.pls.core.component3
import icu.windea.pls.core.component4
import icu.windea.pls.core.math.NumberConverter
import icu.windea.pls.core.math.formatted
import icu.windea.pls.core.removePrefixOrNull
import java.awt.Color

@Suppress("UseJBColor")
@Optimized
object ColorService {
    // TODO 3.0.3 add tests

    /**
     * 得到 hex 格式的颜色。
     *
     * 颜色参数的格式：
     * - 以 `0x` 开始的十六进制字符串，忽略大小写。
     */
    fun getColorFromHex(colorArg: String): Color? {
        if (!checkColorArg(colorArg)) return null
        val hex = colorArg.removePrefixOrNull("0x", ignoreCase = true) ?: return null
        return ColorUtil.fromHex(hex)
    }

    /**
     * 根据作为参考的 [colorArg] 以及输入的 [newColor]，得到期望的 hex 格式的颜色参数。
     *
     * 示例：：
     * - 以 `0x` 开始的十六进制字符串，忽略大小写。
     */
    fun getNewColorArgFromHex(colorArg: String, newColor: Color): String? {
        if (!checkColorArg(colorArg)) return null
        val withAlpha = (colorArg.length - 2) % 4 == 0
        val hex = ColorUtil.toHex(newColor, withAlpha)
        return "0x${hex}"
    }

    /**
     * 根据输入的 [colorArgs]，得到 rgb 格式的颜色。
     *
     * 颜色参数的格式：
     * - `$r $g $b` - 分别匹配区间 `[0..255]` 或 `[0.0..1.0]`。
     * - `$r $g $b $a` - 分别匹配区间 `[0..255]` 或 `[0.0..1.0]`。
     */
    fun getColorFromRgb(colorArgs: List<String>): Color? {
        if (!checkColorArgs(colorArgs)) return null
        val useFloat = colorArgs.allFast { it.toFloat() in 0f..1f } && colorArgs.anyFast { it.contains('.') }
        if (useFloat) {
            val r = NumberConverter.convertFloatToInt(colorArgs.get(0), 255, 0..255) { it * 255 }
            val g = NumberConverter.convertFloatToInt(colorArgs.get(1), 255, 0..255) { it * 255 }
            val b = NumberConverter.convertFloatToInt(colorArgs.get(2), 255, 0..255) { it * 255 }
            val a = NumberConverter.convertFloatToInt(colorArgs.getOrNull(3), 255) { it * 255 } // alpha can overflow
            return Color(r, g, b, a)
        } else {
            val r = NumberConverter.convertIntToInt(colorArgs.get(0), 255, 0..255)
            val g = NumberConverter.convertIntToInt(colorArgs.get(1), 255, 0..255)
            val b = NumberConverter.convertIntToInt(colorArgs.get(2), 255, 0..255)
            val a = NumberConverter.convertIntToInt(colorArgs.getOrNull(3), 255) // alpha can overflow
            return Color(r, g, b, a)
        }
    }

    /**
     * 根据作为参考的 [colorArgs] 以及输入的 [newColor]，得到期望的 rgb 格式的颜色参数。
     * 通过 [precision] 指定浮点数参数的精确度，默认保留3位小数。
     *
     * 颜色参数的格式：
     * - `$r $g $b` - 分别匹配区间 `[0..255]` 或 `[0.0..1.0]`。
     * - `$r $g $b $a` - 分别匹配区间 `[0..255]` 或 `[0.0..1.0]`。
     */
    fun getNewColorArgsFromRgb(colorArgs: List<String>, newColor: Color, precision: Int = -3): List<String>? {
        if (!checkColorArgs(colorArgs)) return null
        val useFloat = colorArgs.anyFast { it.contains('.') } && colorArgs.allFast { it.toFloat() in 0f..1f }
        val withAlpha = colorArgs.size == 4
        val (r, g, b, a) = newColor
        val list = if (withAlpha) listOf(r, g, b, a) else listOf(r, g, b)
        if (useFloat) {
            return list.mapFast { (it / 255f).formatted(precision) }
        } else {
            return list.mapFast { it.toString() }
        }
    }

    /**
     * 根据输入的 [colorArgs]，得到 hsv 格式的颜色。
     *
     * 颜色参数的格式：
     * - `$h $s $v` - 分别匹配区间 `[0.0..1.0]`。
     * - `$h $s $v $a` - 分别匹配区间 `[0.0..1.0]`。
     */
    fun getColorFromHsv(colorArgs: List<String>): Color? {
        if (!checkColorArgs(colorArgs)) return null
        val h = NumberConverter.convertFloatToFloat(colorArgs.get(0), 1f, 0f..1f)
        val s = NumberConverter.convertFloatToFloat(colorArgs.get(1), 1f, 0f..1f)
        val v = NumberConverter.convertFloatToFloat(colorArgs.get(2), 1f, 0f..1f)
        val a = NumberConverter.convertFloatToInt(colorArgs.getOrNull(3), 255) { it * 255 } // alpha can overflow
        val (r, g, b) = Color.getHSBColor(h, s, v)
        return Color(r, g, b, a)
    }

    /**
     * 根据作为参考的 [colorArgs] 以及输入的 [newColor]，得到期望的 hsv 格式的颜色参数。
     * 通过 [precision] 指定浮点数参数的精确度，默认保留3位小数。
     *
     * 颜色参数的格式：
     * - `$h $s $v` - 分别匹配区间 `[0.0..1.0]`。
     */
    fun getNewColorArgsFromHsv(colorArgs: List<String>, newColor: Color, precision: Int = -3): List<String>? {
        if (!checkColorArgs(colorArgs)) return null
        val withAlpha = colorArgs.size == 4
        val (r, g, b) = newColor
        val (h, s, v) = Color.RGBtoHSB(r, g, b, null)
        val a = newColor.alpha / 255f
        val list = if (withAlpha) listOf(h, s, v, a) else listOf(h, s, v)
        return list.mapFast { it.formatted(precision) }
    }

    /**
     * 根据输入的 [colorArgs]，得到 hsv360 格式的颜色。
     *
     * 颜色参数的格式：
     * - `$h $s $v` - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`。
     * - `$h $s $v $a` - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`，`$a` 匹配区间 `[0..255]`。
     */
    fun getColorFromHsv360(colorArgs: List<String>): Color? {
        if (!checkColorArgs(colorArgs)) return null
        val h = NumberConverter.convertIntToFloat(colorArgs.get(0), 1f, 0f..1f) { it / 360 }
        val s = NumberConverter.convertIntToFloat(colorArgs.get(1), 1f, 0f..1f) { it / 100 }
        val v = NumberConverter.convertIntToFloat(colorArgs.get(2), 1f, 0f..1f) { it / 100 }
        val a = NumberConverter.convertIntToInt(colorArgs.getOrNull(3), 255) // alpha can overflow
        val (r, g, b) = Color.getHSBColor(h, s, v)
        return Color(r, g, b, a)
    }

    /**
     * 根据作为参考的 [colorArgs] 以及输入的 [newColor]，得到期望的 hsv360 格式的颜色参数。
     *
     * 颜色参数的格式：
     * - `$h $s $v` - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`。
     * - `$h $s $v $a` - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`，`$a` 匹配区间 `[0..255]`。
     */
    fun getNewColorArgsFromHsv360(colorArgs: List<String>, newColor: Color): List<String>? {
        if (!checkColorArgs(colorArgs)) return null
        val withAlpha = colorArgs.size == 4
        val (r, g, b) = newColor
        val (h0, s0, v0) = Color.RGBtoHSB(r, g, b, null)
        val h = h0 * 360
        val s = s0 * 100
        val v = v0 * 100
        val a = newColor.alpha
        val list = if (withAlpha) listOf(h, s, v, a) else listOf(h, s, v)
        return list.mapFast { it.toString() }
    }

    private fun checkColorArg(colorArg: String): Boolean {
        val hex = colorArg.removePrefixOrNull("0x", ignoreCase = true) ?: return false
        val length = hex.length
        return length == 3 || length == 4 || length == 6 || length == 8
    }

    private fun checkColorArgs(colorArgs: List<String>): Boolean {
        val size = colorArgs.size
        return size == 3 || size == 4
    }
}
