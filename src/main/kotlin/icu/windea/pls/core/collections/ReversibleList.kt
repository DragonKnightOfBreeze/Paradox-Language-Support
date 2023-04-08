@file:Suppress("unused")

package icu.windea.pls.core.collections

data class ReversibleList<T>(val list: List<T>, val notReversed: Boolean) : List<T> by list {
	override fun contains(element: T): Boolean {
		return if(notReversed) list.contains(element) else !list.contains(element)
	}
	
	override fun containsAll(elements: Collection<T>): Boolean {
		return if(notReversed) list.containsAll(elements) else !list.containsAll(elements)
	}
}

fun <T> List<T>.toReversibleList(notReversed: Boolean) = ReversibleList(this, notReversed)