@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core

import com.google.common.collect.ImmutableList
import icu.windea.pls.core.annotations.Fast

/**
 * 对 [String.equals] 的 ASCII 比较版本。
 *
 * ## 实现笔记
 *
 * - 手写逐字符比较，并在遇到非 ASCII 差异时回退到标准 `String.equals`。
 * - 基准显示短 ASCII 和非 ASCII 比较通常慢于标准实现，`ignoreCase` 基本持平，长 ASCII 明显更慢。
 * - 标准实现直接比较压缩字节数组，并完整支持 Unicode，因此没有继续使用此实现的必要。
 *
 * ## 弃用声明
 *
 * 此方法已弃用。应改用标准的字符串比较，因为标准实现通常拥有更好的性能。
 *
 * @see kotlin.text.equals
 */
@Deprecated(
    message = "Use standard `String.equals` instead; the JDK implementation is faster and handles Unicode.",
    replaceWith = ReplaceWith("this.equals(other, ignoreCase)"),
    level = DeprecationLevel.ERROR,
)
@Fast
fun String?.equalsFast(other: String?, ignoreCase: Boolean = false): Boolean {
    if (this === null) return other === null
    @Suppress("StringReferentialEquality")
    if (this === other) return true
    if (other === null) return false

    val length = length
    if (length != other.length) return false

    for (i in 0 until length) {
        val c1 = this[i]
        val c2 = other[i]
        if (c1 == c2) continue

        if (c1.code > 127 || c2.code > 127) return equals(other, ignoreCase)

        val lc1 = if (ignoreCase && c1 in 'A'..'Z') c1.code + 32 else c1.code
        val lc2 = if (ignoreCase && c2 in 'A'..'Z') c2.code + 32 else c2.code

        if (lc1 != lc2) return false
    }
    return true
}

/**
 * 检查当前字符串是否等于数组中的任意字符串。
 *
 * ## 弃用声明
 *
 * 此方法已弃用。应改用标准的字符串比较，因为标准实现通常拥有更好的性能。
 *
 * @see kotlin.text.equals
 */
@Deprecated(
    message = "Use standard `any { it.equals(...) }` instead.",
    replaceWith = ReplaceWith("other.any { it.equals(this, ignoreCase) }"),
    level = DeprecationLevel.ERROR,
)
@Fast
fun String.equalsAnyFast(other: Array<String>, ignoreCase: Boolean = false): Boolean {
    return other.any { it.equals(this, ignoreCase) }
}

/**
 * 检查数组中是否存在字符串等于当前字符串。
 *
 * ## 弃用声明
 *
 * 此方法已弃用。应改用标准的字符串比较，因为标准实现通常拥有更好的性能。
 *
 * @see kotlin.text.equals
 */
@Deprecated(
    message = "Use standard `any { it.equals(...) }` instead.",
    replaceWith = ReplaceWith("any { it.equals(other, ignoreCase) }"),
    level = DeprecationLevel.ERROR,
)
@Fast
fun Array<String>.equalsAnyFast(other: String, ignoreCase: Boolean = false): Boolean {
    return any { it.equals(other, ignoreCase) }
}

/** @see kotlin.text.trim */
@Fast
fun String.trimFast(c: Char): String {
    var startIndex = 0
    var endIndex = length - 1
    var startFound = false
    while (startIndex <= endIndex) {
        val index = if (!startFound) startIndex else endIndex
        val match = this[index] == c
        if (!startFound) {
            if (!match)
                startFound = true
            else
                startIndex += 1
        } else {
            if (!match)
                break
            else
                endIndex -= 1
        }
    }
    return substring(startIndex, endIndex + 1)
}

/** @see kotlin.text.split */
@Fast
fun String.splitFast(delimiter: Char = ',', ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    require(limit >= 0) { "Limit must be non-negative, but was $limit" }

    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
    if (nextIndex == -1 || limit == 1) {
        val r = this
        return ImmutableList.of(r)
    }

    val isLimited = limit > 0
    val result = ArrayList<String>(if (isLimited) limit.coerceAtMost(10) else 10)
    do {
        val r = substring(currentOffset, nextIndex)
        result.add(r)
        currentOffset = nextIndex + 1
        // Do not search for next occurrence if we're reaching limit
        if (isLimited && result.size == limit - 1) break
        nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
    } while (nextIndex != -1)

    val r = substring(currentOffset, length)
    result.add(r)
    return result
}

/** @see kotlin.collections.joinToString */
@Fast
fun <T> List<T>.joinToStringFast(separator: CharSequence = ", ", transform: ((T) -> CharSequence)? = null): String {
    val size = size
    if (size == 0) return ""
    if (size == 1) return this[0].withTransform(transform).toString()
    val builder = StringBuilder()
    var marker = false
    for (i in 0 until size) {
        val e = this[i]
        if (marker) builder.append(separator) else marker = true
        builder.append(e.withTransform(transform))
    }
    return builder.toString()
}

private fun <T> T.withTransform(transform: ((T) -> CharSequence)? = null): CharSequence {
    return if (transform == null) toString() else transform(this)
}
