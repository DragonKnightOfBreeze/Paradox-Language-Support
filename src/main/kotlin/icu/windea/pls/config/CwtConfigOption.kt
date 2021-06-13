package icu.windea.pls.config

data class CwtConfigOption(
	val key:String,
	val separator: CwtConfigSeparator = CwtConfigSeparator.EQUAL,
	val value:String,
	var booleanValue:Boolean? = null,
	var intValue:Int? = null,
	var floatValue:Float? = null,
	var stringValue:String? = null,
	var values:List<CwtConfigOptionValue>? = null,
	var options:List<CwtConfigOption>? = null
)