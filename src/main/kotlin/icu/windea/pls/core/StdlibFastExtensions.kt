package icu.windea.pls.core

import it.unimi.dsi.fastutil.objects.*

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

fun CharSequence.splitFast(delimiter: Char, ignoreCase: Boolean = false, limit: Int = 0): List<String> {
    require(limit >= 0) { "Limit must be non-negative, but was $limit" }

    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
    if (nextIndex == -1 || limit == 1) {
        return listOf(this.toString())
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
