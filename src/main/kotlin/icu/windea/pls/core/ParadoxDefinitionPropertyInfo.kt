package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*

@Suppress("unused")
class ParadoxDefinitionPropertyInfo(
	val elementPath: ParadoxDefinitionPropertyPath,
	val scope: String? = null,
	val gameType: ParadoxGameType,
	val definitionInfo: ParadoxDefinitionInfo,
	private val configGroup: CwtConfigGroup,
	private val pointer: SmartPsiElementPointer<ParadoxDefinitionProperty>,
) {
	val propertyConfigs: List<CwtPropertyConfig> by lazy { resolvePropertyConfigs() } //基于keyExpression，valueExpression可能不同
	val propertyConfig: CwtPropertyConfig? by lazy { resolvePropertyConfig() }
	val matchedPropertyConfig: CwtPropertyConfig? by lazy { resolveMatchedPropertyConfig() }
	val childPropertyConfigs: List<CwtPropertyConfig> by lazy { resolveChildPropertyConfigs() } //基于上一级keyExpression，keyExpression一定唯一
	val childValueConfigs: List<CwtValueConfig> by lazy { resolveChildValuesConfigs() } //基于上一级keyExpression，valueExpression一定唯一
	val childPropertyOccurrence: Map<CwtKeyExpression, Int> by lazy { resolveChildPropertyOccurrence() }
	val childValueOccurrence: Map<CwtValueExpression, Int> by lazy { resolveChildValueOccurrence() }
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionPropertyInfo && elementPath == other.elementPath
	}
	
	override fun hashCode(): Int {
		return Objects.hash(elementPath)
	}
	
	/**
	 * 根据路径解析对应的属性配置列表。
	 */
	private fun resolvePropertyConfigs(): List<CwtPropertyConfig> {
		return definitionInfo.definitionConfig?.resolvePropertyConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	private fun resolvePropertyConfig(): CwtPropertyConfig? {
		//如果propertyConfigs不为空，则直接取第一个
		return propertyConfigs.firstOrNull()
	}
	
	private fun resolveMatchedPropertyConfig(): CwtPropertyConfig? {
		//NOTE 如果变更了其他definitionProperty导致definition的类型发生变更，valueExpression会过时
		//需要匹配value
		val element = pointer.element ?: return null
		if(element !is ParadoxScriptProperty) return null
		val propertyValue = element.propertyValue ?: return null
		if(propertyConfigs.isEmpty()) return null
		return propertyConfigs.find { matchesValue(it.valueExpression, propertyValue.value, configGroup) }
	}
	
	/**
	 * 根据路径解析对应的子属性配置列表。（过滤重复的）
	 */
	private fun resolveChildPropertyConfigs(): List<CwtPropertyConfig> {
		return definitionInfo.definitionConfig?.resolveChildPropertyConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	private fun resolveChildValuesConfigs(): List<CwtValueConfig> {
		return definitionInfo.definitionConfig?.resolveChildValuesConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	/**
	 * 解析子属性基于配置的出现次数。
	 */
	private fun resolveChildPropertyOccurrence(): Map<CwtKeyExpression, Int> {
		val properties = pointer.element?.properties ?: return emptyMap()
		if(properties.isEmpty()) return emptyMap()
		return properties.groupAndCountBy { prop ->
			childPropertyConfigs.find { matchesKey(it.keyExpression, prop.propertyKey, configGroup) }?.keyExpression
		}
	}
	
	/**
	 * 解析子值基于配置的出现次数。
	 */
	private fun resolveChildValueOccurrence(): Map<CwtValueExpression, Int> {
		val values = pointer.element?.values ?: return emptyMap()
		if(values.isEmpty()) return emptyMap()
		return values.groupAndCountBy { value ->
			childValueConfigs.find { matchesValue(it.valueExpression, value, configGroup) }?.valueExpression
		}
	}
}