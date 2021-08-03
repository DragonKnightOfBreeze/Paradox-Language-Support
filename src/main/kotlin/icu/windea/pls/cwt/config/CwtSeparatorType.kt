package icu.windea.pls.cwt.config

import icu.windea.pls.*

enum class CwtSeparatorType(
	override val key: String,
	override val text: String
) : Enumerable {
	EQUAL("=", "Equal"),
	NOT_EQUAL("<>", "Not Equal");
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		fun resolve(key: String): CwtSeparatorType? {
			return when(key) {
				"=", "==" -> EQUAL
				"<>", "!=" -> NOT_EQUAL
				else -> null
			}
		}
	}
}