package icu.windea.pls.config

data class CwtDefinitionConfig(
	val name:String,
	val propertiesConfig:List<CwtConfigProperty>,
	val subtypePropertiesConfig:Map<String,List<CwtConfigProperty>>
)