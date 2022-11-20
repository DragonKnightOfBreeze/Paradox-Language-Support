package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*

data class CwtPropertyConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val key: String,
	override val value: String,
	override val booleanValue: Boolean? = null,
	override val intValue: Int? = null,
	override val floatValue: Float? = null,
	override val stringValue: String? = null,
	override val properties: List<CwtPropertyConfig>? = null,
	override val values: List<CwtValueConfig>? = null,
	override val documentation: String? = null,
	override val options: List<CwtOptionConfig>? = null,
	override val optionValues: List<CwtOptionValueConfig>? = null,
	val separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : CwtDataConfig<CwtProperty>() {
	//val stringValues by lazy { values?.mapNotNull { it.stringValue } }
	//val stringValueOrValues by lazy { stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue } }
	
	val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
	val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.EmptyExpression else CwtValueExpression.resolve(value)
	override val expression: CwtKeyExpression get() = keyExpression
	
	val valueConfig by lazy {
		val resolvedPointer = resolved().pointer
		val valuePointer = resolvedPointer.containingFile
			?.let { f -> resolvedPointer.element?.propertyValue?.createPointer(f) } ?: return@lazy null
		CwtValueConfig(
			valuePointer, info, value, booleanValue, intValue, floatValue, stringValue,
			properties, values, documentation, options, optionValues
		).also { it.parent = parent }
	}
	
	override fun resolved(): CwtPropertyConfig = inlineableConfig?.config ?: this
	
	override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config
	
	//规则内联相关
	
	var inlineableConfig: CwtInlineableConfig? = null
	
	/**
	 * 从[singleAliasConfig]内联规则：value改为取[singleAliasConfig]的的value，如果需要拷贝，则进行深拷贝。
	 */
	fun inlineFromSingleAliasConfig(singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
		//内联所有value
		val other = singleAliasConfig.config
		val inlined = copy(
			value = other.value,
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			properties = other.deepCopyProperties(),
			values = other.deepCopyValues()
		)
		inlined.parent = parent
		inlined.properties?.forEach { it.parent = inlined }
		inlined.values?.forEach { it.parent = inlined }
		inlined.inlineableConfig = singleAliasConfig
		return inlined
	}
	
	/**
	 * 从[aliasConfig]内联规则：key改为取[aliasConfig]的subName，value改为取[aliasConfig]的的value，如果需要拷贝，则进行深拷贝。
	 */
	fun inlineFromAliasConfig(aliasConfig: CwtAliasConfig): CwtPropertyConfig {
		//内联所有value，key取aliasSubName（如：alias[effect:if] 中的if）
		val other = aliasConfig.config
		val inlined = copy(
			key = aliasConfig.subName,
			value = other.value,
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			properties = other.deepCopyProperties(),
			values = other.deepCopyValues()
		)
		inlined.parent = parent
		inlined.properties?.forEach { it.parent = inlined }
		inlined.values?.forEach { it.parent = inlined }
		inlined.inlineableConfig = aliasConfig
		return inlined
	}
}
