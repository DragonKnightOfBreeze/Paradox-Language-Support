package icu.windea.pls.config

import icu.windea.pls.*

data class CwtConfigOption(
	val key: String,
	val separator: CwtConfigSeparator = CwtConfigSeparator.EQUAL,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtConfigOptionValue>? = null,
	val options: List<CwtConfigOption>? = null
) {
	val stringValues = values?.mapNotNull { it.stringValue }
	val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
}