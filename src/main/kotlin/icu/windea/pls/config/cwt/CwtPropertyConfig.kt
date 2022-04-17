package icu.windea.pls.config.cwt

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
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
	val keyExpression: CwtKeyExpression,
	val valueExpression: CwtValueExpression
) : CwtKvConfig<CwtProperty>() {
	override var parent: CwtPropertyConfig? = null
	
	val stringValues = values?.mapNotNull { it.stringValue }
	val stringValueOrValues = stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue }
	
	//懒加载
	val valueConfig by lazy { doGetValueConfig() }
	
	private fun doGetValueConfig(): CwtValueConfig? {
		val valuePointer = pointer.containingFile?.let { f -> pointer.element?.value?.createPointer(f) } ?: return null
		return CwtValueConfig(
			valuePointer, value, booleanValue, intValue, floatValue, stringValue,
			values, properties, documentation, options, optionValues, valueExpression
		).also { it.parent = parent }
	}
}

