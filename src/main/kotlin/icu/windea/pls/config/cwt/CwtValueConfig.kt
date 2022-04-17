package icu.windea.pls.config.cwt

import com.intellij.psi.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtValueConfig(
	override val pointer: SmartPsiElementPointer<CwtValue>,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val values: List<CwtValueConfig>? = null,
	val properties: List<CwtPropertyConfig>? = null,
	override val documentation: String? = null,
	override val options: List<CwtOptionConfig>? = null,
	override val optionValues: List<CwtOptionValueConfig>? = null,
	val valueExpression: CwtValueExpression
): CwtKvConfig<CwtValue>(){
	override var parent: CwtPropertyConfig? = null
	
	//val stringValues = values?.mapNotNull { it.stringValue }
	//val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
}