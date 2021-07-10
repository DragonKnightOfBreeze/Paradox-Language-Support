package icu.windea.pls.model

import icu.windea.pls.*

enum class SeparatorType(
	override val key: String,
	override val text: String
) : Enumerable {
	EQUAL("=", "Equal"),
	NOT_EQUAL("<>", "Not Equal");
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		fun resolve(key: String): SeparatorType? {
			return when(key) {
				"=", "==" -> EQUAL
				"<>", "!=" -> NOT_EQUAL
				else -> null
			}
		}
	}
}