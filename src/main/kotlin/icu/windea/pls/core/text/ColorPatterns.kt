@file:Suppress("unused")

package icu.windea.pls.core.text

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

/**
 * @see ColorPattern
 */
@Suppress("UseJBColor")
object ColorPatterns {
    /** 所有默认的颜色模式。 */
    val patterns: List<ColorPattern<*>> = listOf(Hex, Rgb, Hsv, Hsv360)

    /** 根据名称得到颜色模式。 */
    fun byName(name: String): ColorPattern<*>? = patterns.find { it.name == name }

    /**
     * hex 颜色模式。
     *
     * 颜色参数的格式：以 `0x` 开始的十六进制字符串，忽略大小写，支持 3/4/6/8 位。
     */
    object Hex : ColorPattern.Inline("hex") {
        override fun isAvailable(args: String): Boolean {
            val hex = args.removePrefixOrNull("0x", ignoreCase = true) ?: return false
            val length = hex.length
            if (length != 3 && length != 4 && length != 6 && length != 8) return false
            if (hex.any { !TextMatcher.isHexDigitChar(it) }) return false
            return true
        }

        override fun isValid(args: String): Boolean {
            // hex 颜色参数只包含格式要求，不涉及区间
            return isAvailable(args)
        }

        override fun getColor(args: String): Color? {
            if (!isAvailable(args)) return null
            val hex = args.removePrefixOrNull("0x", ignoreCase = true) ?: return null
            return ColorUtil.fromHex(hex, null)
        }

        override fun getColorArgs(color: Color, referenceArgs: String): String? {
            if (!isAvailable(referenceArgs)) return null
            val withAlpha = (referenceArgs.length - 2) % 4 == 0
            val hex = ColorUtil.toHex(color, withAlpha)
            return "0x${hex}"
        }
    }

    /**
     * rgb 颜色模式。
     *
     * 颜色参数的格式：
     * - `$r $g $b` - 分别匹配区间 `[0..255]` 或 `[0.0..1.0]`。
     * - `$r $g $b $a` - 分别匹配区间 `[0..255]` 或 `[0.0..1.0]`。
     */
    object Rgb : ColorPattern.Block("rgb") {
        @Optimized
        override fun isValid(args: List<String>): Boolean {
            // 如果任一参数包含小数点，则视为浮点表示，所有参数需匹配区间 `[0.0..1.0]`
            // 否则视为整数表示，所有参数需匹配区间 `[0..255]`
            if (!isAvailable(args)) return false
            return if (args.anyFast { it.contains('.') }) {
                // 浮点表示
                args.allFast { it.toFloatOrNull()?.let { v -> v in 0f..1f } == true }
            } else {
                // 整数表示
                args.allFast { it.toIntOrNull()?.let { v -> v in 0..255 } == true }
            }
        }

        @Optimized
        override fun getColor(args: List<String>): Color? {
            if (!isAvailable(args)) return null
            val useFloat = args.anyFast { it.contains('.') } // check dot only is enough here (since color args are checked before)
            if (useFloat) {
                val r = args.get(0).toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
                val g = args.get(1).toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
                val b = args.get(2).toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
                val a = args.getOrNull(3)?.toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
                return Color(r, g, b, a)
            } else {
                val r = args.get(0).toIntOrNull().convertToInt(255, 0..255)
                val g = args.get(1).toIntOrNull().convertToInt(255, 0..255)
                val b = args.get(2).toIntOrNull().convertToInt(255, 0..255)
                val a = args.getOrNull(3)?.toIntOrNull().convertToInt(255, 0..255)
                return Color(r, g, b, a)
            }
        }

        @Optimized
        override fun getColorArgs(color: Color, referenceArgs: List<String>): List<String>? {
            if (!isAvailable(referenceArgs)) return null
            val useFloat = referenceArgs.anyFast { it.contains('.') }
            val withAlpha = referenceArgs.size == 4
            val (r, g, b, a) = color
            val list = if (withAlpha) listOf(r, g, b, a) else listOf(r, g, b)
            return if (useFloat) list.mapFast { (it / 255f).formatted() } else list.mapFast { it.toString() }
        }
    }

    /**
     * hsv 颜色模式。
     *
     * 颜色参数的格式：
     * - `$h $s $v` - 分别匹配区间 `[0.0..1.0]`。
     * - `$h $s $v $a` - 分别匹配区间 `[0.0..1.0]`。
     */
    object Hsv : ColorPattern.Block("hsv") {
        @Optimized
        override fun isValid(args: List<String>): Boolean {
            // 所有参数需匹配区间 `[0.0..1.0]`
            if (!isAvailable(args)) return false
            return args.allFast { it.toFloatOrNull()?.let { v -> v in 0f..1f } == true }
        }

        override fun getColor(args: List<String>): Color? {
            if (!isAvailable(args)) return null
            val h = args.get(0).toFloatOrNull().convertToFloat(1f, 0f..1f)
            val s = args.get(1).toFloatOrNull().convertToFloat(1f, 0f..1f)
            val v = args.get(2).toFloatOrNull().convertToFloat(1f, 0f..1f)
            val a = args.getOrNull(3)?.toFloatOrNull().convertToInt(255, 0..255) { it * 255 }
            val (r, g, b) = Color.getHSBColor(h, s, v)
            return Color(r, g, b, a)
        }

        @Optimized
        override fun getColorArgs(color: Color, referenceArgs: List<String>): List<String>? {
            if (!isAvailable(referenceArgs)) return null
            val withAlpha = referenceArgs.size == 4
            val (r, g, b) = color
            val (h, s, v) = Color.RGBtoHSB(r, g, b, null)
            val a = color.alpha / 255f
            val list = if (withAlpha) listOf(h, s, v, a) else listOf(h, s, v)
            return list.mapFast { it.formatted() }
        }
    }

    /**
     * hsv360 颜色模式。
     *
     * 颜色参数的格式：
     * - `$h $s $v` - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`。
     * - `$h $s $v $a` - `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`，`$a` 匹配区间 `[0..255]`。
     */
    object Hsv360 : ColorPattern.Block("hsv360") {
        override fun isValid(args: List<String>): Boolean {
            // 所有参数需为整数
            // `$h` 匹配区间 `[0..360]`，`$s` `$v` 匹配区间 `[0..100]`，`$a` 匹配区间 `[0..255]`
            if (!isAvailable(args)) return false
            val h = args.get(0).toIntOrNull()?.let { it in 0..360 } == true
            val s = args.get(1).toIntOrNull()?.let { it in 0..100 } == true
            val v = args.get(2).toIntOrNull()?.let { it in 0..100 } == true
            val a = args.getOrNull(3)?.let { it.toIntOrNull()?.let { alpha -> alpha in 0..255 } == true } ?: true
            return h && s && v && a
        }

        override fun getColor(args: List<String>): Color? {
            if (!isAvailable(args)) return null
            val h = args.get(0).toIntOrNull().convertToFloat(1f, 0f..1f) { it / 360f }
            val s = args.get(1).toIntOrNull().convertToFloat(1f, 0f..1f) { it / 100f }
            val v = args.get(2).toIntOrNull().convertToFloat(1f, 0f..1f) { it / 100f }
            val a = args.getOrNull(3)?.toIntOrNull().convertToInt(255, 0..255)
            val (r, g, b) = Color.getHSBColor(h, s, v)
            return Color(r, g, b, a)
        }

        @Optimized
        override fun getColorArgs(color: Color, referenceArgs: List<String>): List<String>? {
            if (!isAvailable(referenceArgs)) return null
            val withAlpha = referenceArgs.size == 4
            val (r, g, b) = color
            val (h0, s0, v0) = Color.RGBtoHSB(r, g, b, null)
            val h = h0 * 360
            val s = s0 * 100
            val v = v0 * 100
            val a = color.alpha
            val list = if (withAlpha) listOf(h, s, v, a) else listOf(h, s, v)
            return list.mapFast { it.toString() }
        }
    }
}
