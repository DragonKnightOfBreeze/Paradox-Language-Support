package icu.windea.pls.config

enum class CwtConfigSeparator(val key:String) {
	EQUAL("="),NOT_EQUAL("<>");
	
	override fun toString(): String {
		return key
	}
	
	companion object{
		fun resolve(key:String): CwtConfigSeparator {
			return when(key){
				"=" -> EQUAL
				"<>","!=" -> NOT_EQUAL
				else -> EQUAL
			}
		}
	}
}