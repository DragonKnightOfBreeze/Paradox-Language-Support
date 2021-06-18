package icu.windea.pls.config

data class CwtConfigFile(
	val values: List<CwtConfigValue>,
	val properties: List<CwtConfigProperty>
){
	companion object{
		val EmptyCwtConfig = CwtConfigFile(emptyList(), emptyList())
	}
}






