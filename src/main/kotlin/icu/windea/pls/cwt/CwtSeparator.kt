package icu.windea.pls.cwt

enum class CwtSeparator(val text: String) {
	EQUAL("="),
	NOT_EQUAL("==");
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		fun resolve(text: String): CwtSeparator? {
			return when(text) {
				"=", "==" -> EQUAL
				"<>", "!=" -> NOT_EQUAL
				else -> null
			}
		}
	}
}