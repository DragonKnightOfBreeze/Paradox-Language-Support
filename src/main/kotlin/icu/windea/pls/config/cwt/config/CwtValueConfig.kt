package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtValueConfig(
	override val pointer: SmartPsiElementPointer<CwtValue>,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	override val properties: List<CwtPropertyConfig>? = null,
	override val values: List<CwtValueConfig>? = null,
	override val documentation: String? = null,
	override val options: List<CwtOptionConfig>? = null,
	override val optionValues: List<CwtOptionValueConfig>? = null
) : CwtKvConfig<CwtValue>() {
	//不显示标注的option和optionValue
	val valueConfigExpression = value
	
	//val stringValues by lazy { values?.mapNotNull { it.stringValue } }
	//val stringValueOrValues by lazy { stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue } }
	
	val valueExpression: CwtValueExpression = CwtValueExpression.resolve(stringValue.orEmpty())
	override val expression: CwtValueExpression get() = valueExpression
	
	override val resolved: CwtValueConfig get() = this
}