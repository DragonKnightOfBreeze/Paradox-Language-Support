package icu.windea.pls.core.model

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.matchesScriptExpression
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
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
	
	private val configs: List<CwtDataConfig<*>> by lazy {
		doGetConfigs(definitionInfo, this, CwtConfigMatchType.ALL)
	}
	
	private val childPropertyConfigs: List<CwtPropertyConfig> by lazy { 
		doGetChildPropertyConfigs(definitionInfo, this, CwtConfigMatchType.ALL)
	}
	
	private val childValueConfigs: List<CwtValueConfig> by lazy {
		doGetChildValueConfigs(definitionInfo, this, CwtConfigMatchType.ALL)
	}
	
	/** 对应的属性/值配置列表。 */
	fun getConfigs(matchType: Int = CwtConfigMatchType.ALL): List<CwtDataConfig<*>> {
		if(matchType == CwtConfigMatchType.ALL) return configs
		return doGetConfigs(definitionInfo, this, matchType)
	}
	
	/** 对应的子属性配置列表。 */
	fun getChildPropertyConfigs(matchType: Int = CwtConfigMatchType.ALL): List<CwtPropertyConfig> {
		if(matchType == CwtConfigMatchType.ALL) return childPropertyConfigs
		return doGetChildPropertyConfigs(definitionInfo, this, matchType)
	}
	
	/** 对应的子值配置列表。 */
	fun getChildValueConfigs(matchType: Int = CwtConfigMatchType.ALL): List<CwtValueConfig> {
		if(matchType == CwtConfigMatchType.ALL) return childValueConfigs
		return doGetChildValueConfigs(definitionInfo, this, matchType)
	}
	
	/** 子属性基于配置的出现次数。 */
	val childPropertyOccurrence: Map<CwtKeyExpression, Int> by lazy {
		doGetChildPropertyOccurrence(element)
	}
	
	/** 子值基于配置的出现次数。 */
	val childValueOccurrence: Map<CwtValueExpression, Int> by lazy {
		doGetChildValueOccurrence(element)
	}
	
}

/**
 * 根据路径解析对应的属性/值配置列表。
 */
private fun doGetConfigs(definitionInfo: ParadoxDefinitionInfo, definitionElementInfo: ParadoxDefinitionElementInfo, matchType: Int): List<CwtDataConfig<*>> {
	//基于keyExpression，valueExpression可能不同
	val declaration = definitionInfo.declaration ?: return emptyList()
	//如果路径中可能待遇参数，则不进行解析
	val elementPath = definitionElementInfo.elementPath
	if(elementPath.isParameterAware) return emptyList()
	val configGroup = definitionElementInfo.configGroup
	if(elementPath.isEmpty()) return declaration.toSingletonList()
	var result = declaration.configs ?: return emptyList()
	var index = 0
	while(index < elementPath.length) {
		val (key, isQuoted, isKey) = elementPath.subPathInfos[index]
		var nextIndex = index + 1
		
		//如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
		
		val expression = ParadoxDataExpression.resolve(key, isQuoted, true)
		val nextResult = SmartList<CwtDataConfig<*>>()
		for(config in result) {
			if(index == 0) {
				if(isKey && config is CwtPropertyConfig) {
					if(matchesScriptExpression(expression, config.keyExpression, configGroup, matchType)) {
						nextIndex = CwtConfigHandler.inlineConfig(key, isQuoted, config, configGroup, nextResult, index, matchType)
					}
				} else if(!isKey && config is CwtValueConfig) {
					nextResult.add(config)
				}
			} else {
				val propertyConfigs = config.properties
				if(isKey && propertyConfigs != null && propertyConfigs.isNotEmpty()) {
					for(propertyConfig in propertyConfigs) {
						if(matchesScriptExpression(expression, propertyConfig.keyExpression, configGroup, matchType)) {
							nextIndex = CwtConfigHandler.inlineConfig(key, isQuoted, propertyConfig, configGroup, nextResult, index, matchType)
						}
					}
				}
				val valueConfigs = config.values
				if(!isKey && valueConfigs != null && valueConfigs.isNotEmpty()) {
					for(valueConfig in valueConfigs) {
						nextResult.add(valueConfig)
					}
				}
			}
		}
		
		//如果存在可以静态匹配（CwtConfigMatchType.STATIC）的规则，则仅选用可以静态匹配的规则
		result = nextResult.filter {
			matchesScriptExpression(expression, it.expression, configGroup, CwtConfigMatchType.STATIC)
		}.ifEmpty { nextResult }
		index = nextIndex
	}
	return result.sortedByPriority(configGroup) { it.expression }
}

/**
 * 根据路径解析对应的子属性配置列表。（过滤重复的）
 */
private fun doGetChildPropertyConfigs(definitionInfo: ParadoxDefinitionInfo, definitionElementInfo: ParadoxDefinitionElementInfo, matchType: Int): List<CwtPropertyConfig> {
	//基于上一级keyExpression，keyExpression一定唯一
	if(definitionInfo.declaration?.configs.isNullOrEmpty()) return emptyList()
	//如果路径中可能待遇参数，则不进行解析
	val elementPath = definitionElementInfo.elementPath
	if(elementPath.isParameterAware) return emptyList()
	//parentPath可以对应property或者value
	return when {
		//这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
		elementPath.isEmpty() -> definitionInfo.declaration?.properties.orEmpty()
		else -> {
			//打平propertyConfigs中的每一个properties
			val propertyConfigs = doGetConfigs(definitionInfo, definitionElementInfo, matchType)
			val result = SmartList<CwtPropertyConfig>()
			for(propertyConfig in propertyConfigs) {
				val props = propertyConfig.properties
				if(props != null && props.isNotEmpty()) result.addAll(props)
			}
			result
		}
	}
}

/**
 * 根据路径解析对应的子值配置列表。（过滤重复的）
 */
private fun doGetChildValueConfigs(definitionInfo: ParadoxDefinitionInfo, definitionElementInfo: ParadoxDefinitionElementInfo, matchType: Int): List<CwtValueConfig> {
	//基于上一级keyExpression，valueExpression一定唯一
	if(definitionInfo.declaration?.configs.isNullOrEmpty()) return emptyList()
	//如果路径中可能待遇参数，则不进行解析
	val elementPath = definitionElementInfo.elementPath
	if(elementPath.isParameterAware) return emptyList()
	//parentPath可以对应property或者value
	return when {
		//这里的属性路径可以为空，这时得到的就是顶级值列表（定义的代码块类型的值中的值列表）
		elementPath.isEmpty() -> definitionInfo.declaration?.values.orEmpty()
		else -> {
			//打平propertyConfigs中的每一个values
			val propertyConfigs = doGetConfigs(definitionInfo, definitionElementInfo, matchType)
			val result = SmartList<CwtValueConfig>()
			for(propertyConfig in propertyConfigs) {
				val values = propertyConfig.values
				if(values != null && values.isNotEmpty()) result.addAll(values)
			}
			result
		}
	}
}

private fun ParadoxDefinitionElementInfo.doGetChildPropertyOccurrence(element: PsiElement): Map<CwtKeyExpression, Int> {
	val properties = when {
		element is ParadoxScriptPropertyKey -> element.propertyValue?.castOrNull<ParadoxScriptBlock>()?.propertyList ?: return emptyMap()
		else -> return emptyMap()
	}
	if(properties.isEmpty()) return emptyMap()
	return properties.groupAndCountBy { prop ->
		val expression = ParadoxDataExpression.resolve(prop.propertyKey)
		getChildPropertyConfigs().find { matchesScriptExpression(expression, it.keyExpression, configGroup) }?.keyExpression
	}
}

private fun ParadoxDefinitionElementInfo.doGetChildValueOccurrence(element: PsiElement): Map<CwtValueExpression, Int> {
	val values = when {
		element is ParadoxScriptPropertyKey -> element.propertyValue?.castOrNull<ParadoxScriptBlock>()?.valueList ?: return emptyMap()
		element is ParadoxScriptBlockElement -> element.valueList
		else -> return emptyMap()
	}
	if(values.isEmpty()) return emptyMap()
	return values.groupAndCountBy { value ->
		val expression = ParadoxDataExpression.resolve(value)
		getChildValueConfigs().find { matchesScriptExpression(expression, it.valueExpression, configGroup) }?.valueExpression
	}
}