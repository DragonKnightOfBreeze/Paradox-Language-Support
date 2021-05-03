package com.windea.plugin.idea.pls.config

data class CwtConfigOption(
	val key:String,
	val value:String?,
	val values:List<CwtConfigOptionValue>?,
	val options:List<CwtConfigOption>?,
	val separator: CwtConfigSeparator = CwtConfigSeparator.EQUAL
)