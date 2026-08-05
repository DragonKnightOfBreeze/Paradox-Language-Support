@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import icu.windea.pls.core.annotations.Fast

/**
 * 判断当前路径是否匹配另一个路径（相同或者是其父路径）。
 * 使用 "/" 作为路径分隔符。
 * 不会忽略前导的路径分隔符。
 *
 * @param other 另一个路径。
 * @param acceptSelf 是否接受路径完全一致的情况。
 * @param strict 是否严格匹配（相同或是其直接父路径）。
 * @param trim 是否需要事先去除当前路径首尾的路径分隔符。不会去除另一个路径首尾的路径分隔符。
 */
@Fast
fun String.matchesPath(other: String, acceptSelf: Boolean = true, strict: Boolean = false, trim: Boolean = false): Boolean {
    // 这个方法的执行速度应当非常非常快

    val path = if (trim) this.trimFast('/') else this
    val length = path.length
    val otherLength = other.length
    if (length > otherLength) return false
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    if ((other as java.lang.String).startsWith(path, 0)) {
        if (length == otherLength) return acceptSelf
        if (other[length] != '/') return false
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        if (strict && (other as java.lang.String).indexOf(47, length + 1) != -1) return false // 47 -> '/'
        return true
    }
    return false
}

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
    val size = size
    if (size == 0) return "" // optimize: fast return
    if (size == 1) return single() // optimize: fast return
    if (this is List) return joinToStringFast(delimiter.toString())
    return joinToString(delimiter.toString())
}
