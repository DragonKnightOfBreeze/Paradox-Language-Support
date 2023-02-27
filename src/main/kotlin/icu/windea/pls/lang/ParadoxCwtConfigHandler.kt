package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

@Suppress("UNCHECKED_CAST")
object ParadoxCwtConfigHandler {
	@JvmStatic
	fun getConfigs(element: PsiElement, allowDefinition: Boolean = element is ParadoxScriptValue, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.DEFAULT): List<CwtDataConfig<*>> {
		return when {
			element is ParadoxScriptDefinitionElement -> getPropertyConfigs(element, allowDefinition, orDefault, matchType)
			element is ParadoxScriptPropertyKey -> getPropertyConfigs(element, allowDefinition, orDefault, matchType)
			element is ParadoxScriptValue -> getValueConfigs(element, allowDefinition, orDefault, matchType)
			else -> emptyList()
		}
	}
	
	@JvmStatic
	fun getPropertyConfigs(element: PsiElement, allowDefinition: Boolean = false, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.DEFAULT): List<CwtPropertyConfig> {
		return getConfigsFromCache(element, CwtPropertyConfig::class.java, allowDefinition, orDefault, matchType)
	}
	
	@JvmStatic
	fun getValueConfigs(element: PsiElement, allowDefinition: Boolean = true, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.DEFAULT): List<CwtValueConfig> {
		return getConfigsFromCache(element, CwtValueConfig::class.java, allowDefinition, orDefault, matchType)
	}
	
	private fun <T: CwtConfig<*>> getConfigsFromCache(element: PsiElement, configType: Class<T>, allowDefinition: Boolean, orDefault: Boolean, matchType: Int): List<T> {
		val configsMap = getConfigsMapFromCache(element) ?: return emptyList()
		val key = buildString { 
			when(configType) {
				CwtPropertyConfig::class.java -> append("property")
				CwtValueConfig::class.java -> append("value")
				else -> throw UnsupportedOperationException()
			}
			append("#").append(allowDefinition.toIntString())
			append("#").append(orDefault.toIntString())
			append("#").append(matchType)
		}
		return configsMap.getOrPut(key) { resolveConfigs(element, configType, allowDefinition, orDefault, matchType) } as List<T>
	}
	
	private fun getConfigsMapFromCache(element: PsiElement): MutableMap<String, List<CwtConfig<*>>>? {
		val file = element.containingFile ?: return null
		if(file !is ParadoxScriptFile) return null
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsMapKey) {
			val value = ConcurrentHashMap<String, List<CwtConfig<*>>>()
			//TODO 需要确定最合适的依赖项
			//invalidated on file modification or ScriptFileTracker
			val tracker = ParadoxModificationTrackerProvider.getInstance().ScriptFile
			CachedValueProvider.Result.create(value, file, tracker)
		}
	}
	
	private fun <T : CwtConfig<*>> resolveConfigs(element: PsiElement, configType: Class<T>, allowDefinition: Boolean, orDefault: Boolean, matchType: Int): List<T> {
		//当输入的元素是key或property时，输入的规则类型必须是property
		return when(configType) {
			CwtPropertyConfig::class.java -> {
				val memberElement = when{
					element is ParadoxScriptDefinitionElement -> element
					element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty ?: return emptyList()
					else -> throw UnsupportedOperationException()
				}
				val expression = when{
					element is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxDataExpression.resolve(it) }
					element is ParadoxScriptFile -> BlockParadoxDataExpression 
					element is ParadoxScriptPropertyKey -> element.propertyValue?.let { ParadoxDataExpression.resolve(it) }
					else -> throw UnsupportedOperationException()
				}
				val definitionMemberInfo = memberElement.definitionMemberInfo ?: return emptyList()
				if(!allowDefinition && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
				
				//如果无法匹配value，则取第一个
				val configs = definitionMemberInfo.getConfigs(matchType)
				val configGroup = definitionMemberInfo.configGroup
				buildList {
					//不完整的属性 - 不匹配值
					if(expression == null) {
						for(config in configs) {
							if(config !is CwtPropertyConfig) continue
							add(config)
						}
						return@buildList
					}
					//精确匹配
					for(config in configs) {
						if(config !is CwtPropertyConfig) continue
						if(CwtConfigHandler.matchesScriptExpression(memberElement, expression, config.valueExpression, config, configGroup, matchType)) {
							add(config)
						}
					}
					//精确匹配无结果 - 不精确匹配
					if(isEmpty()) {
						val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
						for(config in configs) {
							if(config !is CwtPropertyConfig) continue
							val configExpression = config.valueExpression
							if(!CwtConfigHandler.requireNotExactMatch(configExpression)) continue
							if(CwtConfigHandler.matchesScriptExpression(memberElement, expression, configExpression, config, configGroup, newMatchType)) {
								add(config)
							}
						}
					}
					//仍然无结果 - 判断是否使用默认值
					if(orDefault && isEmpty()) {
						configs.forEach { it.castOrNull<CwtPropertyConfig>()?.let { c -> add(c) } }
					}
				} as List<T>
			}
			CwtValueConfig::class.java -> {
				val valueElement = when {
					element is ParadoxScriptValue -> element
					else -> throw UnsupportedOperationException()
				}
				val expression = ParadoxDataExpression.resolve(valueElement)
				val parent = element.parent
				when(parent) {
					//如果value是property的value
					is ParadoxScriptProperty -> {
						val property = parent
						val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
						if(!allowDefinition && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
						
						ProgressManager.checkCanceled()
						val configs = definitionMemberInfo.getConfigs(matchType)
						val configGroup = definitionMemberInfo.configGroup
						buildList {
							//精确匹配
							for(config in configs) {
								if(config !is CwtPropertyConfig) continue
								val valueConfig = config.valueConfig ?:  continue
								if(CwtConfigHandler.matchesScriptExpression(valueElement, expression, valueConfig.expression, config, configGroup, matchType)) {
									add(valueConfig)
								}
							}
							//精确匹配无结果 - 不精确匹配
							if(isEmpty()) {
								val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
								for(config in configs) {
									if(config !is CwtPropertyConfig) continue
									val valueConfig = config.valueConfig ?:  continue
									val configExpression = valueConfig.expression
									if(!CwtConfigHandler.requireNotExactMatch(configExpression)) continue
									if(CwtConfigHandler.matchesScriptExpression(valueElement, expression, configExpression, config, configGroup, newMatchType)) {
										add(valueConfig)
									}
								}
							}
							//仍然无结果 - 判断是否使用默认值
							if(orDefault && isEmpty()) {
								configs.forEach { it.castOrNull<CwtPropertyConfig>()?.valueConfig?.let { c -> add(c) } }
							}
						} as List<T>
					}
					//如果value是blockElement中的value
					is ParadoxScriptBlockElement -> {
						val property = parent.parent as? ParadoxScriptDefinitionElement ?: return emptyList()
						val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
						
						val childConfigs = definitionMemberInfo.getChildConfigs(matchType)
						if(childConfigs.isEmpty()) return emptyList()
						val configGroup = definitionMemberInfo.configGroup
						buildList {
							for(childConfig in childConfigs) {
								if(childConfig !is CwtValueConfig) continue
								//精确匹配
								if(CwtConfigHandler.matchesScriptExpression(valueElement, expression, childConfig.valueExpression, childConfig, configGroup, matchType)) {
									add(childConfig)
								}
							}
							//精确匹配无结果 - 不精确匹配
							if(isEmpty()) {
								val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
								for(childConfig in childConfigs) {
									if(childConfig !is CwtValueConfig) continue
									val configExpression = childConfig.valueExpression
									if(!CwtConfigHandler.requireNotExactMatch(configExpression)) continue
									if(CwtConfigHandler.matchesScriptExpression(valueElement, expression, configExpression, childConfig, configGroup, newMatchType)) {
										add(childConfig)
									}
								}
							}
							//仍然无结果 - 判断是否使用默认值
							if(orDefault && isEmpty()) {
								childConfigs.singleOrNull { it is CwtValueConfig }?.let { add(it) }
							}
						} as List<T>
					}
					else -> return emptyList()
				}
			}
			else -> throw UnsupportedOperationException()
		}
	}
	
	//DONE 兼容需要考虑内联的情况（如内联脚本）
	//DONE 这里需要兼容匹配key的子句规则有多个的情况 - 匹配任意则使用匹配的首个规则，空子句或者都不匹配则使用合并的规则
	
	/**
	 * 得到指定的[element]的作为值的子句中的子属性/值的出现次数信息。（先合并子规则）
	 */
	@JvmStatic
	fun getChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtDataConfig<*>>): Map<CwtDataExpression, Occurrence> {
		if(configs.isEmpty()) return emptyMap()
		val configGroup = configs.first().info.configGroup
		val project = configGroup.project
		val blockElement = when{
			element is ParadoxScriptDefinitionElement -> element.block
			element is ParadoxScriptBlockElement -> element
			else -> null
		}
		if(blockElement == null) return emptyMap()
		val childConfigs = configs.flatMap { it.configs.orEmpty() }
		val occurrenceMap = mutableMapOf<CwtDataExpression, Occurrence>()
		for(childConfig in childConfigs) {
			occurrenceMap.put(childConfig.expression, childConfig.toOccurrence(element, project))
		}
		ProgressManager.checkCanceled()
		blockElement.processData p@{ data ->
			val expression = when {
				data is ParadoxScriptProperty -> ParadoxDataExpression.resolve(data.propertyKey)
				data is ParadoxScriptValue -> ParadoxDataExpression.resolve(data)
				else -> return@p true
			}
			val isParameterAware = expression.type == ParadoxDataType.StringType && expression.text.isParameterAwareExpression()
			//may contain parameter -> can't and should not get occurrences
			if(isParameterAware) {
				occurrenceMap.clear()
				return@p true
			}
			val matched = childConfigs.find { childConfig ->
				if(childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
				if(childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
				CwtConfigHandler.matchesScriptExpression(data, expression, childConfig.expression, childConfig, configGroup)
			}
			if(matched == null) return@p  true
			val occurrence = occurrenceMap[matched.expression]
			if(occurrence == null) return@p true
			occurrence.actual += 1
			true
		}
		return occurrenceMap
	}
}
