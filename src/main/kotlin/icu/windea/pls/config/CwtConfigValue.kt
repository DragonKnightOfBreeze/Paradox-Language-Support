package icu.windea.pls.config

data class CwtConfigValue(
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtConfigValue>? = null,
	val properties: List<CwtConfigProperty>? = null,
	val documentation: String? = null,
	val options: List<CwtConfigOption>? = null,
	val optionValues: List<CwtConfigOptionValue>? = null
)