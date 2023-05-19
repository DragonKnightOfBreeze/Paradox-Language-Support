package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtPropertyConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val key: String,
	override val value: String,
	override val booleanValue: Boolean? = null,
	override val intValue: Int? = null,
	override val floatValue: Float? = null,
	override val stringValue: String? = null,
	override val configs: List<CwtDataConfig<*>>? = null,
	override val documentation: String? = null,
	override val options: List<CwtOptionConfig>? = null,
	override val optionValues: List<CwtOptionValueConfig>? = null,
	val separatorType: CwtSeparator = CwtSeparator.EQUAL,
) : CwtDataConfig<CwtProperty>() {
	companion object {
		val Empty = CwtPropertyConfig(emptyPointer(), CwtConfigGroupInfo(""), "", "")
	}
	
	//val stringValues by lazy { values?.mapNotNull { it.stringValue } }
	//val stringValueOrValues by lazy { stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue } }
	
	val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
	val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
	
	override val expression: CwtKeyExpression get() = keyExpression
	
	val valueConfig by lazy {
		val valuePointer = when {
			pointer == emptyPointer<CwtValue>() -> emptyPointer()
			else -> {
				val resolvedPointer = resolved().pointer
				val resolvedFile = resolvedPointer.containingFile ?:return@lazy null
				resolvedPointer.element?.propertyValue?.createPointer(resolvedFile)
			}
		} 
		if(valuePointer == null) return@lazy null
		CwtValueConfig(
			valuePointer, info, value, booleanValue, intValue, floatValue, stringValue,
			configs, documentation, options, optionValues, this
		)
	}
	
	override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
	
	override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
	
	override fun toString(): String {
		return "$key ${separatorType.text} $value"
	}
	
}
