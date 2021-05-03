package com.windea.plugin.idea.pls.config

data class CwtConfigOptionValue(
	val value:String?,
	val values:List<CwtConfigOptionValue>?,
	val options: List<CwtConfigOption>?
)