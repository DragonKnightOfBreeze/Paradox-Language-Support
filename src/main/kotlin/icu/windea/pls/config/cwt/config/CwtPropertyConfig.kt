package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
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
	val separatorType: CwtSeparator,
) : CwtDataConfig<CwtProperty>() {
	//val stringValues by lazy { values?.mapNotNull { it.stringValue } }
	//val stringValueOrValues by lazy { stringValue?.toSingletonList() ?: values?.mapNotNull { it.stringValue } }
	
	val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
	val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
	
	override val expression: CwtKeyExpression get() = keyExpression
	
	val valueConfig by lazy {
		val resolvedPointer = resolved().pointer
		val valuePointer = resolvedPointer.containingFile
			?.let { f -> resolvedPointer.element?.propertyValue?.createPointer(f) } 
			?: return@lazy null
		CwtValueConfig(
			valuePointer, info, value, booleanValue, intValue, floatValue, stringValue,
			configs, documentation, options, optionValues
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
			configs = other.deepCopyConfigs(),
			documentation = other.documentation,
			options = other.options,
			optionValues = other.optionValues
		)
		inlined.parent = parent
		inlined.configs?.forEach { it.parent = inlined }
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
			configs = other.deepCopyConfigs(),
			documentation = other.documentation,
			options = other.options,
			optionValues = other.optionValues
		)
		inlined.parent = parent
		inlined.configs?.forEach { it.parent = inlined }
		inlined.inlineableConfig = aliasConfig
		return inlined
	}
	
	/**
	 * 将指定的[inlineConfig]内联作为子节点并返回。如果需要拷贝，则进行深拷贝。
	 */
	fun inlineConfigAsChild(inlineConfig: CwtInlineConfig) : CwtPropertyConfig{
		val inlined = inlineConfig.config.copy(
			configs = inlineConfig.config.deepCopyConfigs()
		)
		inlined.parent = this
		inlined.configs?.forEach { it.parent = inlined }
		inlined.inlineableConfig = inlineConfig
		return inlined
	}
}
