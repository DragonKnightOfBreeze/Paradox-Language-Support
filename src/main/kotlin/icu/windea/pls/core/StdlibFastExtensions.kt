@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core

import com.google.common.collect.ImmutableList
import icu.windea.pls.core.annotations.Fast

/**
 * 对 [String.equals] 的 ASCII 比较版本（已弃用）。
 *
 * 性能笔记：
 * - 短 ASCII 比较比标准 `String.equals` 慢约 75%，非 ASCII 比较慢约 64%；
 * - `ignoreCase` 基本持平；
 * - 长 ASCII 比较明显更慢，因为标准实现直接比较压缩字节数组；
 * - 标准实现完整支持 Unicode，而此实现只对 ASCII 做快速路径。
 *
 * 新代码请使用 `==` 或 [String?.equals]。
 *
 * @see kotlin.text.equals
 */
@Deprecated(
    message = "Use standard String.equals instead; the JDK implementation is faster and handles Unicode.",
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

/** @see kotlin.text.equals */
@Fast
fun String.equalsAnyFast(other: Array<String>, ignoreCase: Boolean = false): Boolean {
    return other.any { it.equals(this, ignoreCase) }
}

/** @see kotlin.text.equals */
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
