package com.windea.plugin.idea.pls.model

data class CwtConfigValue(
	val value:String?,
	val values: List<CwtConfigValue>?,
	val properties: List<CwtConfigProperty>?,
	val options: CwtConfigOptions,
	val documentation: String,
)