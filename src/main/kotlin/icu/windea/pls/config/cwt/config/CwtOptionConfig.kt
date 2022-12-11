package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*

data class CwtOptionConfig(
	override val pointer: SmartPsiElementPointer<CwtOption>, //NOTE 未使用
	override val info: CwtConfigInfo,
	val key: String,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val options: List<CwtOptionConfig>? = null,
	val optionValues: List<CwtOptionValueConfig>? = null,
	val separatorType: CwtSeparator = CwtSeparator.EQUAL
) : CwtConfig<CwtOption> {
	//val stringValues = values?.mapNotNull { it.stringValue }
	//val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
}