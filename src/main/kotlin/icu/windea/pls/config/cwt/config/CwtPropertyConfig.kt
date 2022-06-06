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
	override val properties: List<CwtPropertyConfig>? = null,
	override val values: List<CwtValueConfig>? = null,
	override val documentation: String? = null,
	override val options: List<CwtOptionConfig>? = null,
	override val optionValues: List<CwtOptionValueConfig>? = null,
	val separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : CwtKvConfig<CwtProperty>() {
	//不显示标注的option和optionValue、以及block中的嵌套规则
	val propertyConfigExpression = "$key = $value"
	val keyConfigExpression = key
	val valueConfigExpression = value
	
	//val stringValues by lazy { values?.mapNotNull { it.stringValue } }
	//val stringValueOrValues by lazy { stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue } }
	
	val keyExpression: CwtKeyExpression by lazy { CwtKeyExpression.resolve(key) }
	val valueExpression: CwtValueExpression by lazy { CwtValueExpression.resolve(stringValue.orEmpty()) }
	
	override val resolved: CwtPropertyConfig get() = inlineableConfig?.config ?: this
	val keyResolved: CwtPropertyConfig get() = inlineableConfig?.castOrNull<CwtAliasConfig>()?.config ?: this
	
	val valueConfig by lazy { doGetValueConfig() }
	
	private fun doGetValueConfig(): CwtValueConfig? {
		val resolvedPointer = resolved.pointer
		val valuePointer = resolvedPointer.containingFile?.let { f -> resolvedPointer.element?.value?.createPointer(f) } ?: return null
		return CwtValueConfig(
			valuePointer, value, booleanValue, intValue, floatValue, stringValue,
			properties, values, documentation, options, optionValues
		).also { it.parent = parent }
	}
	
	//规则内联相关
	//TODO properties和values需要考虑深拷贝
	
	var inlineableConfig: CwtInlineableConfig? = null
	
	/**
	 * 从[singleAliasConfig]内联规则：value改为取[singleAliasConfig]的的value，如果需要拷贝，则进行深拷贝。
	 */
	fun inlineFromSingleAliasConfig(singleAliasConfig: CwtSingleAliasConfig):CwtPropertyConfig{
		//内联所有value
		val other = singleAliasConfig.config
		val inlined = copy(
			value = other.value, 
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			properties = other.properties,
			values = other.values
		)
		inlined.inlineableConfig = singleAliasConfig
		return inlined
	}
	
	/**
	 * 从[aliasConfig]内联规则：key改为取[aliasConfig]的subName，value改为取[aliasConfig]的的value，如果需要拷贝，则进行深拷贝。
	 */
	fun inlineFromAliasConfig(aliasConfig:CwtAliasConfig):CwtPropertyConfig{
		//内联所有value，key取aliasSubName（如：alias[effect:if] 中的if）
		val other = aliasConfig.config
		val inlined = copy(
			key = aliasConfig.subName,
			value = other.value,
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			properties = other.properties,
			values = other.values
		)
		inlined.inlineableConfig = aliasConfig
		return inlined
	}
}
