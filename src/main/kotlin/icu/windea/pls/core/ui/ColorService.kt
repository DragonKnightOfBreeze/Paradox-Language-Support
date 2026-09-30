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
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.core.math.convertToFloat
import icu.windea.pls.core.math.convertToInt
import icu.windea.pls.core.math.formatted
import icu.windea.pls.core.removePrefixOrNull
import java.awt.Color

@Suppress("UseJBColor")
@Optimized
object ColorService {
    /**
     * 得到 hex 格式的颜色。
     *
     * 颜色参数的格式：
     * - 以 `0x` 开始的十六进制字符串，忽略大小写。
     */
    fun getColorFromHex(colorArg: String): Color? {
        if (!isAvailableColorArg(colorArg)) return null
        val hex = colorArg.removePrefixOrNull("0x", ignoreCase = true) ?: return null
        return ColorUtil.fromHex(hex, null)
    }

    /**
     * 根据作为参考的 [colorArg] 以及输入的 [newColor]，得到期望的 hex 格式的颜色参数。
     *
     * 示例：
     * - 以 `0x` 开始的十六进制字符串，忽略大小写。
     */
    fun getNewColorArgFromHex(colorArg: String, newColor: Color): String? {
        if (!isAvailableColorArg(colorArg)) return null
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
     *
     * 备注：
     * - 颜色参数并不要求严格合法，超出区间的参数会被截断。
     */
    fun getColorFromRgb(colorArgs: List<String>): Color? {
        if (!isAvailableColorArgs(colorArgs)) return null
        val useFloat = colorArgs.anyFast { it.contains('.') } // check dot only is enough here (since color args are checked before)
        if (useFloat) {
            val r = colorArgs.get(0).toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
            val g = colorArgs.get(1).toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
            val b = colorArgs.get(2).toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
            val a = colorArgs.getOrNull(3)?.toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
            return Color(r, g, b, a)
        } else {
            val r = colorArgs.get(0).toIntOrNull().convertToInt(255, 0..255)
            val g = colorArgs.get(1).toIntOrNull().convertToInt(255, 0..255)
            val b = colorArgs.get(2).toIntOrNull().convertToInt(255, 0..255)
            val a = colorArgs.getOrNull(3)?.toIntOrNull().convertToInt(255, 0..255)
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
        if (!isAvailableColorArgs(colorArgs)) return null
        val useFloat = colorArgs.anyFast { it.contains('.') } // check dot only is enough here (since color args are checked before)
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
     *
     * 备注：
     * - 颜色参数并不要求严格合法，超出区间的参数会被截断。
     */
    fun getColorFromHsv(colorArgs: List<String>): Color? {
        if (!isAvailableColorArgs(colorArgs)) return null
        val h = colorArgs.get(0).toFloatOrNull().convertToFloat(1f, 0f..1f)
        val s = colorArgs.get(1).toFloatOrNull().convertToFloat(1f, 0f..1f)
        val v = colorArgs.get(2).toFloatOrNull().convertToFloat(1f, 0f..1f)
        val a = colorArgs.getOrNull(3)?.toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
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
        if (!isAvailableColorArgs(colorArgs)) return null
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
     *
     * 备注：
     * - 颜色参数并不要求严格合法，超出区间的参数会被截断。
     */
    fun getColorFromHsv360(colorArgs: List<String>): Color? {
        if (!isAvailableColorArgs(colorArgs)) return null
        val h = colorArgs.get(0).toIntOrNull().convertToFloat(1f, 0f..1f) { it / 360f }
        val s = colorArgs.get(1).toIntOrNull().convertToFloat(1f, 0f..1f) { it / 100f }
        val v = colorArgs.get(2).toIntOrNull().convertToFloat(1f, 0f..1f) { it / 100f }
        val a = colorArgs.getOrNull(3)?.toIntOrNull().convertToInt(255, 0..255)
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
        if (!isAvailableColorArgs(colorArgs)) return null
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

    /**
     * 检查输入的 [colorArg] 是否可用。
     *
     * 可用的颜色参数可以用于得到对应的颜色，但不一定严格合法（可能会使用默认值或者进行截断）。
     */
    fun isAvailableColorArg(colorArg: String): Boolean {
        val hex = colorArg.removePrefixOrNull("0x", ignoreCase = true) ?: return false
        val length = hex.length
        if (length != 3 && length != 4 && length != 6 && length != 8) return false
        if (hex.any { !TextMatcher.isHexDigitChar(it) }) return false
        return true
    }

    /**
     * 检查输入的 [colorArgs] 是否可用。
     *
     * 可用的颜色参数可以用于得到对应的颜色，但不一定严格合法（可能会使用默认值或者进行截断）。
     *
     * 说明：
     * - 要求参数个数为 3 或 4，并且均为数字。
     */
    fun isAvailableColorArgs(colorArgs: List<String>): Boolean {
        val size = colorArgs.size
        if (size != 3 && size != 4) return false
        if (colorArgs.anyFast { !TextMatcher.matchesFloat(it) }) return false
        return true
    }

    /**
     * 检查输入的 [colorArg] 是否严格合法。
     *
     * 说明：
     * - hex 颜色参数只包含格式要求，不涉及区间，因此与 [isAvailableColorArg] 的判定一致。
     */
    fun isValidColorArg(colorArg: String): Boolean {
        return isAvailableColorArg(colorArg)
    }

    /**
     * 检查输入的 [colorArgs] 是否严格合法，要求为合法的 rgb 颜色参数。
     *
     * 说明：
     * - 如果任一参数包含小数点，则视为浮点表示，所有参数需匹配区间 `[0.0..1.0]`。
     * - 否则视为整数表示，所有参数需匹配区间 `[0..255]`。
     */
    fun isValidRgbColorArgs(colorArgs: List<String>): Boolean {
        if (!isAvailableColorArgs(colorArgs)) return false
        return if (colorArgs.anyFast { it.contains('.') }) {
            colorArgs.allFast { it.toFloatOrNull()?.let { v -> v in 0f..1f } == true }
        } else {
            colorArgs.allFast { it.toIntOrNull()?.let { v -> v in 0..255 } == true }
        }
    }

    /**
     * 检查输入的 [colorArgs] 是否严格合法，要求为合法的 hsv 颜色参数。
     *
     * 说明：
     * - 所有参数需匹配区间 `[0.0..1.0]`。
     */
    fun isValidHsvColorArgs(colorArgs: List<String>): Boolean {
        if (!isAvailableColorArgs(colorArgs)) return false
        return colorArgs.allFast { it.toFloatOrNull()?.let { v -> v in 0f..1f } == true }
    }

    /**
     * 检查输入的 [colorArgs] 是否严格合法，要求为合法的 hsv360 颜色参数。
     *
     * 说明：
     * - 所有参数需为整数。
     * - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`，`$a` 匹配区间 `[0..255]`。
     */
    fun isValidHsv360ColorArgs(colorArgs: List<String>): Boolean {
        if (!isAvailableColorArgs(colorArgs)) return false
        val h = colorArgs.get(0).toIntOrNull()?.let { it in 0..360 } == true
        val s = colorArgs.get(1).toIntOrNull()?.let { it in 0..100 } == true
        val v = colorArgs.get(2).toIntOrNull()?.let { it in 0..100 } == true
        val a = colorArgs.getOrNull(3)?.let { it.toIntOrNull()?.let { alpha -> alpha in 0..255 } == true } ?: true
        return h && s && v && a
    }
}
