@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.intellij.openapi.util.text.StringUtilRt
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.isNotNullOrEmpty
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet
import java.util.*

/**
 * 忽略大小写的字符串哈希与相等策略。
 */
object CaseInsensitiveStringHashingStrategy : Hash.Strategy<String?> {
    override fun hashCode(s: String?): Int {
        return if (s == null) 0 else StringUtilRt.stringHashCodeInsensitive(s)
    }

    override fun equals(s1: String?, s2: String?): Boolean {
        return s1.equals(s2, ignoreCase = true)
    }
}

/**
 * 创建一个期望大小为 [expectedSize] 的不可变列表，并通过 [init] 初始化。
 */
inline fun <T : Any> ImmutableList(expectedSize: Int, init: (index: Int) -> T): List<T> {
    require(expectedSize >= 0) { "expectedSize must be non-negative" }
    if (expectedSize == 0) return emptyList()
    val builder = ImmutableList.builderWithExpectedSize<T>(expectedSize)
    repeat(expectedSize) { index -> builder.add(init(index)) }
    return builder.build()
}

/**
 * 创建一个期望大小为 [expectedSize] 的不可变集，并通过 [init] 初始化。
 */
inline fun <T : Any> ImmutableSet(expectedSize: Int, init: (index: Int) -> T): Set<T> {
    require(expectedSize >= 0) { "expectedSize must be non-negative" }
    if (expectedSize == 0) return emptySet()
    val builder = ImmutableSet.builderWithExpectedSize<T>(expectedSize)
    repeat(expectedSize) { index -> builder.add(init(index)) }
    return builder.build()
}

/**
 * 创建一个可变的集（[MutableSet]）。如果指定了 [comparator]，则返回使用此比较器的 [TreeSet]，否则返回默认的 [MutableSet]。
 */
fun <T> MutableSet(comparator: Comparator<T>? = null): MutableSet<T> {
    return if (comparator == null) mutableSetOf() else TreeSet(comparator)
}

/**
 * 创建一个可变的字符串集。如果 [caseInsensitive] 为 `true`，则使用大小写不敏感的哈希策略。
 */
fun MutableStringSet(caseInsensitive: Boolean = false): MutableSet<String> {
    return if (caseInsensitive) CaseInsensitiveStringSet() else mutableSetOf()
}

/**
 * 创建一个可变的键为字符串的映射。如果 [caseInsensitive] 为 `true`，则键使用大小写不敏感的哈希策略。
 */
fun <V> MutableStringKeyMap(caseInsensitive: Boolean = false): MutableMap<String, V> {
    return if (caseInsensitive) CaseInsensitiveStringKeyMap() else mutableMapOf()
}

/**
 * 创建一个字符串集合，且则使用大小写不敏感的哈希策略。
 */
inline fun CaseInsensitiveStringSet(): ObjectLinkedOpenCustomHashSet<@CaseInsensitive String> {
    return ObjectLinkedOpenCustomHashSet<@CaseInsensitive String>(CaseInsensitiveStringHashingStrategy)
}

/**
 * 创建一个键为字符串的映射，且键使用大小写不敏感的哈希策略。
 */
inline fun <V> CaseInsensitiveStringKeyMap(): Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, V> {
    return Object2ObjectLinkedOpenCustomHashMap<@CaseInsensitive String, V>(CaseInsensitiveStringHashingStrategy)
}

/**
 * 将多个集合合并到 [destination]，忽略 `null` 或空集合，返回 [destination] 本身。
 */
fun <T : Any, C : MutableCollection<T>> mergeTo(destination: C, vararg collections: Collection<T>?): C {
    collections.forEach { collection ->
        if (collection.isNotNullOrEmpty()) destination.addAll(collection)
    }
    return destination
}

/**
 * 合并多个集合得到新的 [List]，忽略 `null` 或空集合，返回新创建的 [ArrayList]。
 */
fun <T : Any> merge(vararg collections: Collection<T>?): List<T> {
    return mergeTo(ArrayList(), *collections)
}
