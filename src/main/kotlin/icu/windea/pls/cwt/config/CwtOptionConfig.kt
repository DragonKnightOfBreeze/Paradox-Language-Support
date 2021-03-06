package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

data class CwtOptionConfig(
	override val pointer: SmartPsiElementPointer<CwtOption>, //NOTE 未使用
	val key: String,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtOptionValueConfig>? = null,
	val options: List<CwtOptionConfig>? = null,
	val separatorType: SeparatorType = SeparatorType.EQUAL
) : CwtConfig<CwtOption> {
	val stringValues = values?.mapNotNull { it.stringValue }
	val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
}