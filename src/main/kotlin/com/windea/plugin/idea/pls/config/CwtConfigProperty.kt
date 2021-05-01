package com.windea.plugin.idea.pls.config

data class CwtConfigProperty(
	val key: String,
	val value: String?,
	val values: List<CwtConfigValue>?,
	val properties: List<CwtConfigProperty>?,
	val options: CwtConfigOptions?,
	val documentation: String?
) 