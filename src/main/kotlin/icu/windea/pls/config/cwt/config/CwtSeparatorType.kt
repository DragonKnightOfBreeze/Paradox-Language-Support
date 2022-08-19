package icu.windea.pls.config.cwt.config

import icu.windea.pls.*

enum class CwtSeparatorType(
	val id: String,
	val description: String
)  {
	EQUAL("=", "Equal"),
	NOT_EQUAL("<>", "Not Equal");
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		fun resolve(id: String): CwtSeparatorType? {
			return when(id) {
				"=", "==" -> EQUAL
				"<>", "!=" -> NOT_EQUAL
				else -> null
			}
		}
	}
}