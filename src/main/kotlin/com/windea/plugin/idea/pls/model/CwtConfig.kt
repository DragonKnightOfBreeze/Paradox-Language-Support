package com.windea.plugin.idea.pls.model

data class CwtConfig(
	val values: List<CwtConfigValue>,
	val properties: List<CwtConfigProperty>
){
	companion object{
		val empty = CwtConfig(emptyList(), emptyList()) 
	}
}




