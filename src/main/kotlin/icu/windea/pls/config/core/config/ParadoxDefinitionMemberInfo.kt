package icu.windea.pls.config.core.config

import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

/**
 * @property elementPath 相对于所属定义的定义成员路径。
 */
class ParadoxDefinitionMemberInfo(
	val elementPath: ParadoxElementPath,
	val gameType: ParadoxGameType,
	val definitionInfo: ParadoxDefinitionInfo,
	val configGroup: CwtConfigGroup,
	element: ParadoxScriptMemberElement //直接传入element
) {
	val isDefinition = element is ParadoxScriptDefinitionElement && elementPath.isEmpty()
	val isParameterAware = elementPath.isParameterAware
	
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
	
	/** 子属性基于键表达式的出现次数映射。 */
	val childPropertyOccurrenceMap: Map<CwtKeyExpression, Occurrence> by lazy {
		doGetChildPropertyOccurrenceMap(element)
	}
	
	/** 子值基于值表达式的出现次数映射。 */
	val childValueOccurrenceMap: Map<CwtValueExpression, Occurrence> by lazy {
		doGetChildValueOccurrenceMap(element)
	}
	
}

/**
 * 根据路径解析对应的属性/值配置列表。
 */
private fun doGetConfigs(definitionInfo: ParadoxDefinitionInfo, definitionMemberInfo: ParadoxDefinitionMemberInfo, matchType: Int): List<CwtDataConfig<*>> {
	//基于keyExpression，valueExpression可能不同
	val declaration = definitionInfo.declaration ?: return emptyList()
	//如果路径中可能待遇参数，则不进行解析
	val elementPath = definitionMemberInfo.elementPath
	if(elementPath.isParameterAware) return emptyList()
	val configGroup = definitionMemberInfo.configGroup
	
	var result: List<CwtDataConfig<*>> = declaration.toSingletonList()
	if(elementPath.isEmpty()) return result
	
	var isInlined = false
	
	for((key, isQuoted, isKey) in elementPath) {
		//如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
		//如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则内联此内联规则
		
		val expression = ParadoxDataExpression.resolve(key, isQuoted, true)
		val nextResult = SmartList<CwtDataConfig<*>>()
		for(parentConfig in result) {
			//处理内联规则 不能嵌套内联
			if(!isInlined && isKey && parentConfig is CwtPropertyConfig) {
				isInlined = CwtConfigHandler.inlineConfigAsChild(key, isQuoted, parentConfig, configGroup, nextResult)
				if(isInlined) continue
			}
			
			val configs = parentConfig.configs
			if(configs.isNullOrEmpty()) continue
			for(config in configs) {
				if(isKey && config is CwtPropertyConfig) {
					if(CwtConfigHandler.matchesScriptExpression(null, expression, config.keyExpression, config, configGroup, matchType)) {
						CwtConfigHandler.inlineConfig(key, isQuoted, config, configGroup, nextResult, matchType)
					}
				} else if(!isKey && config is CwtValueConfig) {
					nextResult.add(config)
				}
			}
		}
		
		//如果存在可以静态匹配（CwtConfigMatchType.STATIC）的规则，则仅选用可以静态匹配的规则
		result = nextResult
			.filter { CwtConfigHandler.matchesScriptExpression(null, expression, it.expression, it, configGroup, CwtConfigMatchType.STATIC) }
			.ifEmpty { nextResult }
	}
	
	return result.sortedByPriority(configGroup) { it.expression }
}

/**
 * 根据路径解析对应的子属性配置列表。（过滤重复的）
 */
private fun doGetChildPropertyConfigs(definitionInfo: ParadoxDefinitionInfo, definitionMemberInfo: ParadoxDefinitionMemberInfo, matchType: Int): List<CwtPropertyConfig> {
	//基于上一级keyExpression，keyExpression一定唯一
	if(definitionInfo.declaration?.configs.isNullOrEmpty()) return emptyList()
	//如果路径中可能待遇参数，则不进行解析
	val elementPath = definitionMemberInfo.elementPath
	if(elementPath.isParameterAware) return emptyList()
	//parentPath可以对应property或者value
	return when {
		//这里的属性路径可以为空，这时得到的就是顶级属性列表（定义的代码块类型的值中的属性列表）
		elementPath.isEmpty() -> definitionInfo.declaration?.properties.orEmpty()
		else -> {
			//打平propertyConfigs中的每一个properties
			val propertyConfigs = doGetConfigs(definitionInfo, definitionMemberInfo, matchType)
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
private fun doGetChildValueConfigs(definitionInfo: ParadoxDefinitionInfo, definitionMemberInfo: ParadoxDefinitionMemberInfo, matchType: Int): List<CwtValueConfig> {
	//基于上一级keyExpression，valueExpression一定唯一
	if(definitionInfo.declaration?.configs.isNullOrEmpty()) return emptyList()
	//如果路径中可能待遇参数，则不进行解析
	val elementPath = definitionMemberInfo.elementPath
	if(elementPath.isParameterAware) return emptyList()
	//parentPath可以对应property或者value
	return when {
		//这里的属性路径可以为空，这时得到的就是顶级值列表（定义的代码块类型的值中的值列表）
		elementPath.isEmpty() -> definitionInfo.declaration?.values.orEmpty()
		else -> {
			//打平propertyConfigs中的每一个values
			val propertyConfigs = doGetConfigs(definitionInfo, definitionMemberInfo, matchType)
			val result = SmartList<CwtValueConfig>()
			for(propertyConfig in propertyConfigs) {
				val values = propertyConfig.values
				if(values != null && values.isNotEmpty()) result.addAll(values)
			}
			result
		}
	}
}

private fun ParadoxDefinitionMemberInfo.doGetChildPropertyOccurrenceMap(element: ParadoxScriptMemberElement): Map<CwtKeyExpression, Occurrence> {
	//keyExpression - (expect actual)
	val childPropertyConfigs = getChildPropertyConfigs()
	val occurrenceMap = childPropertyConfigs.associateByTo(mutableMapOf(), { it.keyExpression }, { it.cardinality.toOccurrence() })
	val blockElement = when{
		element is ParadoxScriptDefinitionElement -> element.block
		element is ParadoxScriptBlockElement -> element
		else -> null
	}
	val properties = blockElement?.propertyList.orEmpty()
	if(properties.isNotEmpty()) {
		for(property in properties) {
			val expression = ParadoxDataExpression.resolve(property.propertyKey)
			val isParameterAware = expression.type == ParadoxDataType.StringType && expression.text.isParameterAwareExpression()
			if(isParameterAware) return emptyMap() //may contain parameter -> can't and should not get occurrences
			val matched = childPropertyConfigs.find { CwtConfigHandler.matchesScriptExpression(property, expression, it.keyExpression, it, configGroup) }
			if(matched == null) continue
			val occurrence = occurrenceMap[matched.keyExpression]
			if(occurrence == null) continue
			occurrence.actual += 1
		}
	}
	return occurrenceMap
}

private fun ParadoxDefinitionMemberInfo.doGetChildValueOccurrenceMap(element: ParadoxScriptMemberElement): Map<CwtValueExpression, Occurrence> {
	//valueExpression - (expect actual)
	val childValueConfigs = getChildValueConfigs()
	val occurrenceMap = childValueConfigs.associateByTo(mutableMapOf(), { it.valueExpression }, { it.cardinality.toOccurrence() })
	val blockElement = when{
		element is ParadoxScriptDefinitionElement -> element.block
		element is ParadoxScriptBlockElement -> element
		else -> null
	}
	val values = blockElement?.valueList.orEmpty()
	if(values.isNotEmpty()) {
		for(value in values) {
			val expression = ParadoxDataExpression.resolve(value)
			val isParameterAware = expression.type == ParadoxDataType.StringType && expression.text.isParameterAwareExpression()
			if(isParameterAware) return emptyMap() //may contain parameter -> can't and should not get occurrences
			val matched = childValueConfigs.find { CwtConfigHandler.matchesScriptExpression(value, expression, it.valueExpression, it, configGroup) }
			if(matched == null) continue
			val occurrence = occurrenceMap[matched.valueExpression]
			if(occurrence == null) continue
			occurrence.actual += 1
		}
	}
	return occurrenceMap
}