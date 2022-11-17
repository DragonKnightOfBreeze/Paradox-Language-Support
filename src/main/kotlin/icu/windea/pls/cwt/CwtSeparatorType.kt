package icu.windea.pls.cwt

enum class CwtSeparatorType {
	EQUAL,
	NOT_EQUAL;
	
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