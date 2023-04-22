@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.core

data class Entry<K, V>(
    var key: K,
    var value: V
)

fun <K, V> List<Entry<K, V>>.toMap() = this.associateBy({ it.key }, { it.value })

fun <K, V> List<Entry<K, V>>.toMutableMap() = this.associateByTo(mutableMapOf(), { it.key }, { it.value })

fun <K, V> Map<K, V>.toEntryList() = this.map { Entry(it.key, it.value) }

fun <K, V> Map<K, V>.toMutableEntryList() = this.mapTo(mutableListOf()) { Entry(it.key, it.value) }
