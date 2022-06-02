package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*

data class CwtPropertyConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val key: String,
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
	val separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : CwtKvConfig<CwtProperty>() {
	override var parent: CwtPropertyConfig? = null
	
	//val stringValues by lazy { values?.mapNotNull { it.stringValue } }
	//val stringValueOrValues by lazy { stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue } }
	
	val keyExpression: CwtKeyExpression by lazy { CwtKeyExpression.resolve(key) }
	val valueExpression: CwtValueExpression by lazy { CwtValueExpression.resolve(stringValue.orEmpty()) }
	
	//不显示标注的option和optionValue、以及block中的嵌套规则
	val typeText = "$key = $value"
	
	val valueConfig by lazy { doGetValueConfig() }
	
	private fun doGetValueConfig(): CwtValueConfig? {
		val valuePointer = pointer.containingFile?.let { f -> pointer.element?.value?.createPointer(f) } ?: return null
		return CwtValueConfig(
			valuePointer, value, booleanValue, intValue, floatValue, stringValue,
			values, properties, documentation, options, optionValues
		).also { it.parent = parent }
	}
}
