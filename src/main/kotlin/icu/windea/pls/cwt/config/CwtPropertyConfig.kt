package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

data class CwtPropertyConfig(
	val pointer: SmartPsiElementPointer<CwtProperty>,
	val key: String,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtValueConfig>? = null,
	val properties: List<CwtPropertyConfig>? = null,
	val documentation: String? = null,
	val options: List<CwtOptionConfig>? = null,
	val optionValues: List<CwtOptionValueConfig>? = null,
	val separatorType: SeparatorType = SeparatorType.EQUAL,
	val keyExpression:CwtKeyExpression,
	val valueExpression: CwtValueExpression
) {
	val stringValues = values?.mapNotNull { it.stringValue }
	val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
	val cardinality = options?.find { it.key == "cardinality" }?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
}

