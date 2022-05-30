package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*

@Suppress("unused")
class ParadoxDefinitionElementInfo(
	val elementPath: ParadoxElementPath<ParadoxDefinitionProperty>,
	val scope: String? = null,
	val gameType: ParadoxGameType,
	val definitionInfo: ParadoxDefinitionInfo,
	val configGroup: CwtConfigGroup,
	element: PsiElement //直接传入element
) {
	//NOTE 部分属性需要使用懒加载
	
	/** 对应的属性配置列表。 */
	val propertyConfigs: List<CwtPropertyConfig> by lazy {
		//基于keyExpression，valueExpression可能不同
		definitionInfo.definitionConfig?.resolvePropertyConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	val matchedPropertyConfig: CwtPropertyConfig? by lazy {
		//NOTE 如果变更了其他definitionProperty导致definition的类型发生变更，valueExpression会过时
		//需要匹配value
		if(element !is ParadoxScriptProperty) return@lazy null
		if(propertyConfigs.isEmpty()) return@lazy null
		val propertyValue = element.propertyValue ?: return@lazy null
		propertyConfigs.find { matchesValue(it.valueExpression, propertyValue.value, configGroup) }
	}
	
	/** 对应的子属性配置列表。（过滤重复的） */
	val childPropertyConfigs: List<CwtPropertyConfig> by lazy {
		//基于上一级keyExpression，keyExpression一定唯一
		definitionInfo.definitionConfig?.resolveChildPropertyConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	val childValueConfigs: List<CwtValueConfig> by lazy {
		//基于上一级keyExpression，valueExpression一定唯一
		//根据路径解析对应的子值配置列表。（过滤重复的）
		definitionInfo.definitionConfig?.resolveChildValuesConfigs(definitionInfo.subtypes, elementPath, configGroup) ?: emptyList()
	}
	
	/** 子属性基于配置的出现次数。 */
	val childPropertyOccurrence: Map<CwtKeyExpression, Int> by lazy {
		val properties = when {
			element is ParadoxScriptProperty -> element.propertyList
			else -> return@lazy emptyMap()
		}
		if(properties.isEmpty()) return@lazy emptyMap()
		properties.groupAndCountBy { prop ->
			childPropertyConfigs.find { matchesKey(it.keyExpression, prop.propertyKey, configGroup) }?.keyExpression
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
			childValueConfigs.find { matchesValue(it.valueExpression, value, configGroup) }?.valueExpression
		}
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionElementInfo
			&& elementPath == other.elementPath && gameType == other.gameType && definitionInfo == other.definitionInfo
	}
	
	override fun hashCode(): Int {
		return Objects.hash(elementPath, gameType, definitionInfo)
	}
}