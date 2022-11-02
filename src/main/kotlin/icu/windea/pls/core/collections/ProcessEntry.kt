package icu.windea.pls.core.collections

object ProcessEntry {
	fun <T> T.addTo(destination: MutableCollection<T>) = destination.add(this)
	
	fun <T : Collection<T>> T.addAllTo(destination: MutableCollection<T>) = destination.addAll(this)
	
	fun Any?.end() = true
	
	fun Any?.stop() = false
}