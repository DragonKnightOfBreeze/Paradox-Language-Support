package icu.windea.pls.config.cwt

enum class CwtSeparatorType(val text: String) {
	EQUAL("="),
	NOT_EQUAL("==");
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		fun resolve(text: String): CwtSeparatorType? {
			return when(text) {
				"=", "==" -> EQUAL
				"<>", "!=" -> NOT_EQUAL
				else -> null
			}
		}
	}
}