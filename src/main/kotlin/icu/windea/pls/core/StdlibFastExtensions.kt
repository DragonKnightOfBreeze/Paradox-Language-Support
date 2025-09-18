package icu.windea.pls.core

import it.unimi.dsi.fastutil.objects.ObjectArrayList

/**
 * 去除字符串两端给定字符 [c]，尽量避免不必要的分配。
 *
 * 复杂度 O(n)。
 */
fun String.trimFast(c: Char): String {
    // Should be very fast

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

/**
 * 以单个字符分隔符高效分割字符串。
 *
 * - 等价于 `split(delimiter)` 的轻量实现，避免正则与多次分配；
 * - [limit] 语义与标准库一致（>0 时最多返回 `limit` 段，最后一段包含余下所有内容）。
 */
fun String.splitFast(delimiter: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    // Should be very fast

    require(limit >= 0) { "Limit must be non-negative, but was $limit" }

    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
    if (nextIndex == -1 || limit == 1) {
        return listOf(this)
    }

    val isLimited = limit > 0
    val result = ObjectArrayList<String>(if (isLimited) limit.coerceAtMost(10) else 10)
    do {
        result.add(substring(currentOffset, nextIndex))
        currentOffset = nextIndex + 1
        // Do not search for next occurrence if we're reaching limit
        if (isLimited && result.size == limit - 1) break
        nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
    } while (nextIndex != -1)

    result.add(substring(currentOffset, length))
    return result
}
