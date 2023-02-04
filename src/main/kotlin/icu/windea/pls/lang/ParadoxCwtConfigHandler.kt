package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.psi.*

object ParadoxCwtConfigHandler {
	@JvmStatic
	fun resolveConfigs(element: PsiElement, allowDefinitionSelf: Boolean = element !is ParadoxScriptPropertyKey, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.ALL): List<CwtDataConfig<*>> {
		return when {
			element is ParadoxScriptDefinitionElement -> resolvePropertyConfigs(element, allowDefinitionSelf, orDefault, matchType)
			element is ParadoxScriptPropertyKey -> resolvePropertyConfigs(element, allowDefinitionSelf, orDefault, matchType)
			element is ParadoxScriptValue -> resolveValueConfigs(element, allowDefinitionSelf, orDefault, matchType)
			else -> emptyList()
		}
	}
	
	@JvmStatic
	fun resolvePropertyConfigs(element: PsiElement, allowDefinitionSelf: Boolean = false, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.ALL): List<CwtPropertyConfig> {
		return doResolveConfigs(element, CwtPropertyConfig::class.java, allowDefinitionSelf, orDefault, matchType)
	}
	
	@JvmStatic
	fun resolveValueConfigs(element: PsiElement, allowDefinitionSelf: Boolean = true, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.ALL): List<CwtValueConfig> {
		return doResolveConfigs(element, CwtValueConfig::class.java, allowDefinitionSelf, orDefault, matchType)
	}
	
	@Suppress("UNCHECKED_CAST")
	@JvmStatic
	private fun <T : CwtConfig<*>> doResolveConfigs(element: PsiElement, configType: Class<T>, allowDefinitionSelf: Boolean, orDefault: Boolean, matchType: Int): List<T> {
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
				if(!allowDefinitionSelf && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
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
						if(!allowDefinitionSelf && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
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
						val childValueConfigs = definitionMemberInfo.getChildValueConfigs(matchType)
						if(childValueConfigs.isEmpty()) return emptyList()
						val configGroup = definitionMemberInfo.configGroup
						buildList {
							for(childValueConfig in childValueConfigs) {
								//精确匹配
								if(CwtConfigHandler.matchesScriptExpression(valueElement, expression, childValueConfig.valueExpression, childValueConfig, configGroup, matchType)) {
									add(childValueConfig)
								}
							}
							//精确匹配无结果 - 不精确匹配
							if(isEmpty()) {
								val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
								for(childValueConfig in childValueConfigs) {
									val configExpression = childValueConfig.valueExpression
									if(!CwtConfigHandler.requireNotExactMatch(configExpression)) continue
									if(CwtConfigHandler.matchesScriptExpression(valueElement, expression, configExpression, childValueConfig, configGroup, newMatchType)) {
										add(childValueConfig)
									}
								}
							}
							//仍然无结果 - 判断是否使用默认值
							if(orDefault && isEmpty()) {
								childValueConfigs.singleOrNull()?.let { add(it) }
							}
						} as List<T>
					}
					else -> return emptyList()
				}
			}
			else -> throw UnsupportedOperationException()
		}
	}
}
