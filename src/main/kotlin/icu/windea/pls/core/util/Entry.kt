@file:Suppress("unused")

package icu.windea.pls.core.util

/**
 * 键值对条目。
 *
 * 常用于与 `Map`/列表之间的转换与序列化。
 */
data class Entry<K, V>(
    var key: K,
    var value: V
)

/** 将条目列表转换为不可变 `Map`。*/
fun <K, V> List<Entry<K, V>>.toMap() = this.associateBy({ it.key }, { it.value })

/** 将条目列表转换为可变 `MutableMap`。*/
fun <K, V> List<Entry<K, V>>.toMutableMap() = this.associateByTo(mutableMapOf(), { it.key }, { it.value })

/** 将 `Map` 转换为条目列表。*/
fun <K, V> Map<K, V>.toEntryList() = this.map { Entry(it.key, it.value) }

/** 将 `Map` 转换为可变条目列表。*/
fun <K, V> Map<K, V>.toMutableEntryList() = this.mapTo(mutableListOf()) { Entry(it.key, it.value) }
