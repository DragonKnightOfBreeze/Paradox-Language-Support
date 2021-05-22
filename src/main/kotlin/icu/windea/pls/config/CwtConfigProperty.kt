package icu.windea.pls.config

import icu.windea.pls.*

data class CwtConfigProperty(
	val key: String,
	var booleanValue:Boolean? = null,
	var intValue:Int? = null,
	var floatValue:Float? = null,
	var stringValue: String? = null,
	var values: List<CwtConfigValue>? = null,
	var properties: List<CwtConfigProperty>? = null,
	var documentation: String? = null,
	var options: List<CwtConfigOption>? = null,
	var optionValues:List<CwtConfigOptionValue>? = null
){
	val cardinality = options?.find { it.key == "cardinality" }?.stringValue?.let { s -> RangeExpression.resolve(s) }
}

