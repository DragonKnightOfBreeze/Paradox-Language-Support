package icu.windea.pls.core.match.date

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 带轻量预检查的默认日期匹配器。
 *
 * 说明：
 * - 无效日期的主要成本来自 `DateTimeFormatter` 解析失败时的异常。
 * - 对当前 CWT 中常见的 pattern 使用形态预检查，可以在解析前拒绝明显无效的输入。
 * - 未知 pattern 不做预检查，直接回退到 `DateTimeFormatter`，以保持自定义 pattern 的语义。
 * - 预检查只负责数字和分隔符形态，不负责月份、日期范围等语义校验。
 * - 形态有效但语义无效的日期仍会进入 `DateTimeFormatter` 并可能抛出解析异常。
 */
class DefaultDateMatcher(override val pattern: String) : DateMatcher {
    private val formatter = DateTimeFormatter.ofPattern(pattern)
    private val shape = dateFieldShapes[pattern]

    override fun matches(input: String): Boolean {
        if (input.isEmpty()) return false

        // 对包含非 ASCII 字符的输入跳过预检查，避免误拒绝本地化数字或其他合法输入。
        if (shape != null && input.all { it.code < 128 } && !matchesShape(input, shape)) return false

        return try {
            formatter.parse(input)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }
}

private class DateFieldShape(
    val groupCount: Int,
    val trailingDot: Boolean,
)

private fun matchesShape(input: String, shape: DateFieldShape): Boolean {
    var index = 0
    if (input[index] == '+' || input[index] == '-') index++
    if (index == input.length) return false

    var groupCount = 1
    var lastWasDigit = false
    while (index < input.length) {
        val c = input[index++]
        if (c in '0'..'9') {
            lastWasDigit = true
            continue
        }
        if (c != '.' || !lastWasDigit) return false

        if (index == input.length) return shape.trailingDot && groupCount == shape.groupCount
        groupCount++
        lastWasDigit = false
    }
    return groupCount == shape.groupCount && lastWasDigit
}

private val threePartDateShape = DateFieldShape(3, trailingDot = false)
private val twoPartDateShape = DateFieldShape(2, trailingDot = false)
private val twoPartTrailingDotDateShape = DateFieldShape(2, trailingDot = true)
private val fourPartDateShape = DateFieldShape(4, trailingDot = false)

/**
 * 当前 CWT 配置中已经出现的常见日期 pattern 对应的预检查。
 *
 * 未列出的 pattern 会跳过预检查；这是有意的保守策略：
 * 预检查只能拒绝确定无法被对应 formatter 接受的输入，不能误拒绝自定义格式。
 */
private val dateFieldShapes = mapOf(
    "y.M.d" to threePartDateShape,
    "yyyy.MM.dd" to threePartDateShape,
    "y.m." to twoPartTrailingDotDateShape,
    "y.m" to twoPartDateShape,
    "y.m.d.h" to fourPartDateShape,
)
