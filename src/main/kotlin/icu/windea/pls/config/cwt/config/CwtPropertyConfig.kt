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
	
	val valueConfig by lazy { doGetValueConfig() }
	
	private fun doGetValueConfig(): CwtValueConfig? {
		val valuePointer = pointer.containingFile?.let { f -> pointer.element?.value?.createPointer(f) } ?: return null
		return CwtValueConfig(
			valuePointer, value, booleanValue, intValue, floatValue, stringValue,
			properties, values, documentation, options, optionValues
		).also { it.parent = parent }
	}
	
	//规则内联相关
	//TODO properties和values需要考虑深拷贝
	
	var rawSingleAliasConfig: CwtSingleAliasConfig? = null
	var rawAliasConfig: CwtAliasConfig? = null
	
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
		inlined.rawSingleAliasConfig = singleAliasConfig
		return inlined
	}
	
	fun inlineFromAliasConfig(aliasConfig:CwtAliasConfig):CwtPropertyConfig{
		//内联所有value，key保持不变（如：alias_name[trigger]）
		val other = aliasConfig.config
		val inlined = copy(
			value = other.value,
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			properties = other.properties,
			values = other.values
		)
		inlined.rawAliasConfig = aliasConfig
		return inlined
	}
}
