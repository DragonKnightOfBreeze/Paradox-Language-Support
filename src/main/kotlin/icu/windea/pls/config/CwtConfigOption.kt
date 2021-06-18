package icu.windea.pls.config

import icu.windea.pls.*

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
){
	val stringValues get() = values?.mapNotNull { it.stringValue }
	val stringValueOrValues get() = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
}