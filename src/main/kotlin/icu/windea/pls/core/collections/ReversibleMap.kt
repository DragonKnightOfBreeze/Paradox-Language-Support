@file:Suppress("unused")

package icu.windea.pls.core.collections

data class ReversibleMap<K, V>(val map: Map<K, V>, val notReversed: Boolean = false) : Map<K, V> by map {
	override fun containsKey(key: K): Boolean {
		return if(notReversed) map.containsKey(key) else !map.containsKey(key)
	}
	
	override fun containsValue(value: V): Boolean {
		return if(notReversed) map.containsValue(value) else !map.containsValue(value)
	}
}

fun <K, V> Map<K, V>.toReversibleMap(notReversed: Boolean) = ReversibleMap(this, notReversed)