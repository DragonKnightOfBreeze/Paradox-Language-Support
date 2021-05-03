package com.windea.plugin.idea.pls.config

enum class CwtConfigSeparator(val key:String) {
	EQUAL("="),NOT_EQUAL("<>");
	
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