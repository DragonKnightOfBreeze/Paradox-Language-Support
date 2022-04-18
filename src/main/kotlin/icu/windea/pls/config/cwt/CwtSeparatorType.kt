package icu.windea.pls.config.cwt

import icu.windea.pls.*

enum class CwtSeparatorType(
	override val id: String,
	override val description: String
) : IdAware, DescriptionAware {
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