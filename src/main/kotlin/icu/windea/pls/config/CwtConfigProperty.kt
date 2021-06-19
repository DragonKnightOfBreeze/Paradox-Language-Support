package icu.windea.pls.config

import com.intellij.psi.*
import com.intellij.structuralsearch.plugin.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

data class CwtConfigProperty(
	val pointer: SmartPsiElementPointer<CwtProperty>,
	val key: String,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtConfigValue>? = null,
	val properties: List<CwtConfigProperty>? = null,
	val documentation: String? = null,
	val options: List<CwtConfigOption>? = null,
	val optionValues: List<CwtConfigOptionValue>? = null,
	val separatorType: SeparatorType = SeparatorType.EQUAL,
) {
	val stringValues = values?.mapNotNull { it.stringValue }
	val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
	val cardinality = options?.find { it.key == "cardinality" }?.stringValue?.let { s -> RangeExpression.resolve(s) }
}

