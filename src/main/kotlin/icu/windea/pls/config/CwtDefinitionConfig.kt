package icu.windea.pls.config

data class CwtDefinitionConfig(
	val name:String,
	val propertiesConfig:Map<String,CwtConfigProperty>,
	val subtypePropertiesConfig:Map<String,Map<String,CwtConfigProperty>>
)