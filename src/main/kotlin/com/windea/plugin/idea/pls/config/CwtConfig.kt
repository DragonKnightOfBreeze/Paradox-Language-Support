package com.windea.plugin.idea.pls.config

data class CwtConfig(
	val values: List<CwtConfigValue>,
	val properties: List<CwtConfigProperty>
)

val EmptyCwtConfig = CwtConfig(emptyList(), emptyList())




