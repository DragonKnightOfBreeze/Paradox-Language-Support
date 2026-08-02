@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core.collections

import com.google.common.collect.ImmutableList
import icu.windea.pls.core.annotations.Fast
import it.unimi.dsi.fastutil.objects.ObjectImmutableList

/**
 * 如果当前列表存在指定的作为前缀的子列表 [prefix]（可以为空），则去除并返回。否则，返回 `null`。
 * 如果指定了通配符 [wildcard]，则当前缀中的元素与其相等时，认为总是匹配当前列表中的对应索引的元素。
 */
@Fast
fun <T : Any> List<T>.removePrefixOrNull(prefix: List<T>, wildcard: T? = null): List<T>? {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    val prefixSize = prefix.size
    if (prefixSize == 0) return this // optimize: fast return
    if (this === prefix) return ImmutableList.of() // optimize: fast return
    val resultSize = size - prefixSize
    if (resultSize < 0) return null
    for (i in 0 until prefixSize) { // optimize: use index-based iteration
        val e = prefix[i]
        if (wildcard != null && wildcard == e) continue
        if (e != this[i]) return null
    }

    if (resultSize == 0) return ImmutableList.of() // optimize: fast return
    if (resultSize == 1) return ImmutableList.of(this[size - 1])
    val elements = arrayOfNulls<Any?>(resultSize) // optimize: construct sized array directly for better performance and memory
    for (i in 0 until resultSize) { // optimize: use index-based iteration
        val e = this[i + prefixSize]
        elements[i] = e
    }
    @Suppress("UNCHECKED_CAST")
    elements as Array<out T>
    return ObjectImmutableList(elements) // memory usage: 16 + align8(16 + 4n)
}

/**
 * 如果当前列表存在指定的作为后缀的子列表 [suffix]（可以为空），则去除并返回，否则返回 `null`。
 * 如果指定了通配符 [wildcard]，则当后缀中的元素与其相等时，认为总是匹配当前列表中的对应索引的元素。
 */
@Fast
fun <T : Any> List<T>.removeSuffixOrNull(suffix: List<T>, wildcard: T? = null): List<T>? {
    // note: assume input is `RandomAccess` and is not `CopyOnWriteArrayList`
    val size = size // optimize: cache input size first
    val suffixSize = suffix.size
    if (suffixSize == 0) return this // fast return
    if (this === suffix) return ImmutableList.of() // fast return
    val resultSize = size - suffixSize
    if (resultSize < 0) return null
    for (i in 0 until suffixSize) { // optimize: use index-based iteration
        val e = suffix[i]
        if (wildcard != null && wildcard == e) continue
        if (e != this[size - suffixSize + i]) return null
    }

    if (resultSize == 0) return ImmutableList.of() // optimize: fast return
    if (resultSize == 1) return ImmutableList.of(this[0])
    val elements = arrayOfNulls<Any?>(resultSize) // optimize: construct sized array directly for better performance and memory
    for (i in 0 until resultSize) { // optimize: use index-based iteration
        val e = this[i]
        elements[i] = e
    }
    @Suppress("UNCHECKED_CAST")
    elements as Array<out T>
    return ObjectImmutableList(elements) // memory usage: 16 + align8(16 + 4n)
}
