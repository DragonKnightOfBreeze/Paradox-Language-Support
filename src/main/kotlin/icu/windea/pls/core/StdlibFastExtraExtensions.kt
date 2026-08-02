@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import icu.windea.pls.core.annotations.Fast

/** 将当前的字符串按 [delimiter] 分割并转化为 [List]。自动去除首尾空白并忽略空项。默认使用英文逗号作为分隔符。 */
@Fast
fun String.toDelimitedList(delimiter: Char = ','): List<String> {
    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset)
    if (nextIndex == -1) {
        val r = trim()
        if (r.isNotEmpty()) return ImmutableList.of(r)
        return ImmutableList.of()
    }

    var result: MutableList<String>? = null
    do {
        val r = substring(currentOffset, nextIndex).trim()
        if (r.isNotEmpty()) {
            if (result == null) result = mutableListOf() // delay initialization
            result.add(r)
        }
        currentOffset = nextIndex + 1
        nextIndex = indexOf(delimiter, currentOffset)
    } while (nextIndex != -1)

    val r = substring(currentOffset, length).trim()
    if (r.isNotEmpty()) {
        if (result == null) result = mutableListOf() // delay initialization
        result.add(r)
    }
    return result.orEmpty()
}

/** 将当前的字符串按 [delimiter] 分割并转化为 [Set]。自动去除首尾空白并忽略空项。默认使用英文逗号作为分隔符。 */
@Fast
fun String.toDelimitedSet(delimiter: Char = ','): Set<String> {
    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset)
    if (nextIndex == -1) {
        val r = trim()
        if (r.isNotEmpty()) return ImmutableSet.of(r)
        return ImmutableSet.of()
    }

    var result: MutableSet<String>? = null
    do {
        val r = substring(currentOffset, nextIndex).trim()
        if (r.isNotEmpty()) {
            if (result == null) result = mutableSetOf() // delay initialization
            result.add(r)
        }
        currentOffset = nextIndex + 1
        nextIndex = indexOf(delimiter, currentOffset)
    } while (nextIndex != -1)

    val r = substring(currentOffset, length).trim()
    if (r.isNotEmpty()) {
        if (result == null) result = mutableSetOf() // delay initialization
        result.add(r)
    }
    return result.orEmpty()
}

/** 将当前的字符串按 [delimiter] 分割并加入到 [result]。自动去除首尾空白并忽略空项。默认使用英文逗号作为分隔符。 */
@Fast
fun String.toDelimitedMutableList(result: MutableList<String> = mutableListOf(), delimiter: Char = ','): MutableList<String> {
    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset)
    if (nextIndex == -1) {
        val r = trim()
        if (r.isNotEmpty()) result.add(r)
        return result
    }

    do {
        val r = substring(currentOffset, nextIndex).trim()
        if (r.isNotEmpty()) result.add(r)
        currentOffset = nextIndex + 1
        nextIndex = indexOf(delimiter, currentOffset)
    } while (nextIndex != -1)

    val r = substring(currentOffset, length).trim()
    if (r.isNotEmpty()) result.add(r)
    return result
}

/** 将当前的字符串按 [delimiter] 分割并加入到 [result]。自动去除首尾空白并忽略空项。默认使用英文逗号作为分隔符。 */
@Fast
fun String.toDelimitedMutableSet(result: MutableSet<String> = mutableSetOf(), delimiter: Char = ','): MutableSet<String> {
    var currentOffset = 0
    var nextIndex = indexOf(delimiter, currentOffset)
    if (nextIndex == -1) {
        val r = trim()
        if (r.isNotEmpty()) result.add(r)
        return result
    }

    do {
        val r = substring(currentOffset, nextIndex).trim()
        if (r.isNotEmpty()) result.add(r)
        currentOffset = nextIndex + 1
        nextIndex = indexOf(delimiter, currentOffset)
    } while (nextIndex != -1)

    val r = substring(currentOffset, length).trim()
    if (r.isNotEmpty()) result.add(r)
    return result
}

/** 将当前的字符串集合拼接为按 [delimiter] 分隔后的字符串。默认使用英文逗号作为分隔符。 */
@Fast
fun Collection<String>.toDelimitedString(delimiter: Char = ','): String {
    return if (isEmpty()) "" else if (size == 1) single() else joinToString(",")
}
