package icu.windea.pls.config

data class CwtConfigOptionValue(
	val value:String,
	var booleanValue:Boolean? = null,
	var intValue:Int? = null,
	var floatValue:Float? = null,
	var stringValue:String? = null,
	var values:List<CwtConfigOptionValue>? = null,
	var options: List<CwtConfigOption>? = null
)