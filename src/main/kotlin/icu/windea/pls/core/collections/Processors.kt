@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.collections

fun <T> Iterable<T>.process(processor: ProcessEntry.(T) -> Boolean): Boolean {
	for(e in this) {
		val result = ProcessEntry.processor(e)
		if(!result) return false
	}
	return true
}

fun <K, V> Map<K, V>.process(processor: ProcessEntry.(Map.Entry<K, V>) -> Boolean): Boolean {
	for(entry in this) {
		val result = ProcessEntry.processor(entry)
		if(!result) return false
	}
	return true
}