package icu.windea.pls.model

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*

/**
 * @property elementPath 相对于所属定义的定义元素路径。
 * @property isValid 对应的PSI元素是否是合法的定义元素（在定义声明内，非定义自身）。
 */
class ParadoxDefinitionElementInfo(
	val elementPath: ParadoxElementPath,
	val scope: String? = null,
	val gameType: ParadoxGameType,
	val definitionInfo: ParadoxDefinitionInfo,
	val configGroup: CwtConfigGroup,
	element: PsiElement //直接传入element
) {
	val isValid = element is ParadoxScriptValue || elementPath.isNotEmpty()
	
	//NOTE 部分属性需要使用懒加载
	
	/** 对应的属性/值配置列表。 */
	val configs: List<CwtKvConfig<*>> by lazy {
		//基于keyExpression，valueExpression可能不同
		definitionInfo.declarationConfig?.resolveConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	val propertyConfigs: List<CwtPropertyConfig> by lazy {
		configs.filterIsInstance<CwtPropertyConfig>()
	}
	
	val matchedPropertyConfig: CwtPropertyConfig? by lazy {
		//NOTE 如果变更了其他definitionProperty导致definition的类型发生变更，valueExpression会过时
		if(element !is ParadoxScriptProperty) return@lazy null
		if(configs.isEmpty()) return@lazy null
		val propertyValue = element.propertyValue ?: return@lazy null
		configs.find { it is CwtPropertyConfig && CwtConfigHandler.matchesValue(it.valueExpression, propertyValue.value, configGroup) }?.cast()
	}
	
	val valueConfigs: List<CwtValueConfig> by lazy {
		configs.filterIsInstance<CwtValueConfig>()
	}
	
	val matchedValueConfig: CwtValueConfig? by lazy {
		//NOTE 如果变更了其他definitionProperty导致definition的类型发生变更，valueExpression会过时
		if(element !is ParadoxScriptProperty) return@lazy null
		if(configs.isEmpty()) return@lazy null
		val propertyValue = element.propertyValue ?: return@lazy null
		configs.find { it is CwtValueConfig && CwtConfigHandler.matchesValue(it.valueExpression, propertyValue.value, configGroup) }?.cast()
	}
	
	/** 对应的子属性配置列表。 */
	val childPropertyConfigs: List<CwtPropertyConfig> by lazy {
		//基于上一级keyExpression，keyExpression一定唯一
		definitionInfo.declarationConfig?.resolveChildPropertyConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	/** 对应的子值配置列表。 */
	val childValueConfigs: List<CwtValueConfig> by lazy {
		//基于上一级keyExpression，valueExpression一定唯一
		definitionInfo.declarationConfig?.resolveChildValuesConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	/** 子属性基于配置的出现次数。 */
	val childPropertyOccurrence: Map<CwtKeyExpression, Int> by lazy {
		val properties = when {
			element is ParadoxScriptProperty -> element.propertyList
			else -> return@lazy emptyMap()
		}
		if(properties.isEmpty()) return@lazy emptyMap()
		properties.groupAndCountBy { prop ->
			childPropertyConfigs.find { CwtConfigHandler.matchesKey(it.keyExpression, prop.propertyKey, configGroup) }?.keyExpression
		}
	}
	
	/** 子值基于配置的出现次数。 */
	val childValueOccurrence: Map<CwtValueExpression, Int> by lazy {
		val values = when {
			element is ParadoxScriptProperty -> element.valueList
			element is ParadoxScriptBlock -> element.valueList
			else -> return@lazy emptyMap()
		}
		if(values.isEmpty()) return@lazy emptyMap()
		values.groupAndCountBy { value ->
			childValueConfigs.find { CwtConfigHandler.matchesValue(it.valueExpression, value, configGroup) }?.valueExpression
		}
	}
}