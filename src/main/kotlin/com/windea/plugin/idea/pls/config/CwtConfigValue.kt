package com.windea.plugin.idea.pls.config

data class CwtConfigValue(
	var booleanValue:Boolean? = null,
	var intValue:Int? = null,
	var floatValue:Float? = null,
	var stringValue:String? = null,
	var values: List<CwtConfigValue>? = null,
	var properties: List<CwtConfigProperty>? = null,
	var documentation: String? = null,
	var options: List<CwtConfigOption>? = null,
	var optionValues:List<CwtConfigOptionValue>? = null
)