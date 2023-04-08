@file:Suppress("unused")

package icu.windea.pls.core.collections

data class ReversibleSet<T>(val set: Set<T>, val notReversed: Boolean) : Set<T> by set {
	override fun contains(element: T): Boolean {
		return if(notReversed) set.contains(element) else !set.contains(element)
	}
	
	override fun containsAll(elements: Collection<T>): Boolean {
		return if(notReversed) set.containsAll(elements) else !set.containsAll(elements)
	}
}

fun <T> Set<T>.toReversibleSet(notReversed: Boolean) = ReversibleSet(this, notReversed)