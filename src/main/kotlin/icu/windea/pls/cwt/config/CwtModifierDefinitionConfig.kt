package icu.windea.pls.cwt.config

data class CwtModifierDefinitionConfig(
	val tag:String,
	val categories:String //string | int[2^?]
): CwtHardCodedConfig