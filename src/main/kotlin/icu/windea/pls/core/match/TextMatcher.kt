package icu.windea.pls.core.match

import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.orNull
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

object TextMatcher {
    private val dateFieldFormatters = ConcurrentHashMap<String, DateTimeFormatter>()

    /**
     * 检查 [text] 中 `[start, end)` 区间是否匹配一个整数。
     *
     * 匹配的字符仅包括数字 `0-9`；当 [leadingUnary] 为 `true` 时，允许首字符为 `+` 或 `-`。
     */
    fun matchesInt(text: String, start: Int = 0, end: Int = text.length, leadingUnary: Boolean = true): Boolean {
        require(start >= 0 && end <= text.length && start <= end)
        return doMatchInt(text, start, end, leadingUnary)
    }

    private fun doMatchInt(text: String, start: Int, end: Int, leadingUnary: Boolean): Boolean {
        if (start == end) return false
        if (leadingUnary) {
            val c = text[start]
            if (c == '+' || c == '-') return doMatchInt(text, start + 1, end, false)
        }
        var current = start
        while (current < end) {
            val c = text[current++]
            if (c.isExactDigit()) continue
            return false
        }
        return true
    }

    /**
     * 检查 [text] 中 `[start, end)` 区间是否匹配一个浮点数。
     *
     * 匹配的字符包括数字 `0-9` 与至多一个小数点 `.`；当 [leadingUnary] 为 `true` 时，允许首字符为 `+` 或 `-`。
     *
     * [lenientDot] 控制小数的严格程度：
     * - `true`（默认）：允许如 `.5`、`5.`、`+.5` 等宽松写法；
     * - `false`：要求小数点前后各至少有一个数字。
     */
    fun matchesFloat(text: String, start: Int = 0, end: Int = text.length, leadingUnary: Boolean = true, lenientDot: Boolean = true): Boolean {
        require(start >= 0 && end <= text.length && start <= end)
        return doMatchFloat(text, start, end, leadingUnary, lenientDot)
    }

    private fun doMatchFloat(text: String, start: Int, end: Int, leadingUnary: Boolean, lenientDot: Boolean): Boolean {
        if (start == end) return false
        if (leadingUnary) {
            val c = text[start]
            if (c == '+' || c == '-') return doMatchFloat(text, start + 1, end, false, lenientDot)
        }
        var current = start
        var expectDot = true
        while (current < end) {
            val c = text[current++]
            if (expectDot && c == '.') {
                if (current == start + 1 && current == end) return false
                if (!lenientDot) {
                    if (current == start + 1 || current == end) return false
                }
                expectDot = false
                continue
            }
            if (c.isExactDigit()) continue
            return false
        }
        return true
    }

    /**
     * 检查 [text] 是否匹配一个数字部分为浮点数的百分比值字符串（如 `50.0%`）。
     */
    fun matchesPercentageField(text: String, leadingUnary: Boolean = true, lenientDot: Boolean = true): Boolean {
        return text.length >= 2 && text.last() == '%' && doMatchFloat(text, 0, text.length - 1, leadingUnary, lenientDot)
    }

    /**
     * 检查 [text] 是否匹配一个数字部分为整数的百分比值字符串（如 `50%`）。
     */
    fun matchesIntPercentageField(text: String, leadingUnary: Boolean = true): Boolean {
        return text.length >= 2 && text.last() == '%' && doMatchInt(text, 0, text.length - 1, leadingUnary)
    }

    /**
     * 检查 [text] 是否匹配指定的日期字段格式。
     *
     * [pattern] 为 [DateTimeFormatter] 兼容的日期模式，默认为 `"y.M.d"`（年月日）。
     * 使用 [DateTimeFormatter.parse] 进行解析匹配，解析成功即返回 `true`，异常时返回 `false`。
     */
    fun matchesDateField(text: String, pattern: String? = null): Boolean {
        val pattern = pattern?.orNull() ?: "y.M.d"
        return try {
            val formatter = dateFieldFormatters.getOrPut(pattern) { DateTimeFormatter.ofPattern(pattern) }
            formatter.parse(text)
            true
        } catch (_: Exception) {
            false
        }
    }
}
