package icu.windea.pls.lang.model

abstract class ReferenceKey(
	private val value: Any?
) {
	abstract override fun equals(other: Any?): Boolean
	
	abstract override fun hashCode(): Int
	
	override fun toString(): String {
		return value.toString()
	}
}