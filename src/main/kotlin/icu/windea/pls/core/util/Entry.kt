package icu.windea.pls.core.util

data class Entry<out K, out V>(
    val key: K,
    val value: V
)

fun <K, V> List<Entry<K, V>>.toMap() = this.associateBy({ it.key }, { it.value })

fun <K, V> List<Entry<K, V>>.toMutableMap() = this.associateByTo(mutableMapOf(), { it.key }, { it.value })

fun <K, V> Map<K, V>.toEntryList() = this.map { Entry(it.key, it.value) }

fun <K, V> Map<K, V>.toMutableEntryList() = this.mapTo(mutableListOf()) { Entry(it.key, it.value) }
