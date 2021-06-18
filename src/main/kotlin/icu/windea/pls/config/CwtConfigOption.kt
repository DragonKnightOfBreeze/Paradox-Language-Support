package icu.windea.pls.config

import icu.windea.pls.*
import icu.windea.pls.model.*

data class CwtConfigOption(
	val key: String,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtConfigOptionValue>? = null,
	val options: List<CwtConfigOption>? = null,
	val separatorType: SeparatorType = SeparatorType.EQUAL
) {
	val stringValues = values?.mapNotNull { it.stringValue }
	val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
}