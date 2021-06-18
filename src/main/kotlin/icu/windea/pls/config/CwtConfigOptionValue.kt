package icu.windea.pls.config

data class CwtConfigOptionValue(
	val value:String,
	val booleanValue:Boolean? = null,
	val intValue:Int? = null,
	val floatValue:Float? = null,
	val stringValue:String? = null,
	val values:List<CwtConfigOptionValue>? = null,
	val options: List<CwtConfigOption>? = null
)