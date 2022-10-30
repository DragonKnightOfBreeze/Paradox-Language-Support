package icu.windea.pls.core.model

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.matchesScriptExpression
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.expression.*
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
	
	val valueConfigs: List<CwtValueConfig> by lazy {
		configs.filterIsInstance<CwtValueConfig>()
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
			val expression = ParadoxScriptExpression.resolve(prop.propertyKey)
			childPropertyConfigs.find { matchesScriptExpression(expression, it.keyExpression, configGroup) }?.keyExpression
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
			val expression = ParadoxScriptExpression.resolve(value)
			childValueConfigs.find { matchesScriptExpression(expression, it.valueExpression, configGroup) }?.valueExpression
		}
	}
}