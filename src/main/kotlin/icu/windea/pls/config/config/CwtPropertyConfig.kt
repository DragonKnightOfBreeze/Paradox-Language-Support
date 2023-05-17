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
	private val overriddenValuePointer: SmartPsiElementPointer<CwtValue>? = null
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
			overriddenValuePointer != null -> overriddenValuePointer
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
	
	/**
	 * 从[singleAliasConfig]内联规则：value改为取[singleAliasConfig]的的value，如果需要拷贝，则进行深拷贝。
	 */
	fun inlineFromSingleAliasConfig(singleAliasConfig: CwtSingleAliasConfig): CwtPropertyConfig {
		//内联所有value
		//这里需要优先使用singleAliasConfig的options、optionValues和documentation
		val other = singleAliasConfig.config
		val inlined = copy(
			value = other.value,
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			configs = other.deepCopyConfigs(),
			documentation = documentation ?: other.documentation,
			options = options,
			optionValues = optionValues
		)
		inlined.parent = parent
		inlined.configs?.forEach { it.parent = inlined }
		inlined.inlineableConfig = singleAliasConfig
		return inlined
	}
	
	/**
	 * 从[aliasConfig]内联规则：key改为取[aliasConfig]的subName，value改为取[aliasConfig]的的value，如果需要拷贝，则进行深拷贝。
	 * 
	 * 如果[valueOnly]为`true`，则key改为取当前规则的key，内联后的valuePointer改为取[aliasConfig]的valuePointer。
	 */
	fun inlineFromAliasConfig(aliasConfig: CwtAliasConfig, valueOnly: Boolean = false): CwtPropertyConfig {
		val other = aliasConfig.config
		val inlined = copy(
			key = if(valueOnly) this.key else aliasConfig.subName,
			value = other.value,
			booleanValue = other.booleanValue,
			intValue = other.intValue,
			floatValue = other.floatValue,
			stringValue = other.stringValue,
			configs = other.deepCopyConfigs(),
			documentation = if(valueOnly) documentation else other.documentation,
			options = if(valueOnly) options else other.options,
			optionValues = if(valueOnly) optionValues else other.optionValues,
			overriddenValuePointer = if(valueOnly) aliasConfig.config.valueConfig?.pointer ?: emptyPointer() else null
		)
		inlined.parent = parent
		inlined.configs?.forEach { it.parent = inlined }
		inlined.inlineableConfig = aliasConfig
		return inlined
	}
	
	/**
	 * 将指定的[inlineConfig]内联作为子节点并返回。如果需要拷贝，则进行深拷贝。
	 */
	fun inlineFromInlineConfig(inlineConfig: CwtInlineConfig) : CwtPropertyConfig{
		val inlined = inlineConfig.config.copy(
			configs = inlineConfig.config.deepCopyConfigs()
		)
		inlined.parent = this
		inlined.configs?.forEach { it.parent = inlined }
		inlined.inlineableConfig = inlineConfig
		return inlined
	}
}
