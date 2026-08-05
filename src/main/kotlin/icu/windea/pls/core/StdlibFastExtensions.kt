@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core

import com.google.common.collect.ImmutableList
import icu.windea.pls.core.annotations.Fast

/** @see kotlin.text.equals */
@Fast
fun String?.equalsFast(other: String?, ignoreCase: Boolean = false): Boolean {
    // 专为纯 ASCII 字符串设计的高性能忽略大小写比较
    // 零对象分配，无 Unicode 开销
    // 感谢猫猫！

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

        // 如果不是纯 ASCII，退化为标准库比较（作为安全兜底）
        if (c1.code > 127 || c2.code > 127) return equals(other, ignoreCase)

        // ASCII 快速转小写比较 (大写字母 A-Z 的 ASCII 码加上 32 就是小写)
        val lc1 = if (ignoreCase && c1 in 'A'..'Z') c1.code + 32 else c1.code
        val lc2 = if (ignoreCase && c2 in 'A'..'Z') c2.code + 32 else c2.code

        if (lc1 != lc2) return false
    }
    return true
}

/** @see kotlin.text.equals */
@Fast
fun Array<String>.equalsAnyFast(other: String, ignoreCase: Boolean = false): Boolean {
    return any { it.equalsFast(other, ignoreCase) }
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
fun String.splitFast(delimiter: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
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

@Fast
fun <T> List<T>.joinToStringFast(separator: CharSequence, transform: ((T) -> CharSequence)? = null): String {
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
