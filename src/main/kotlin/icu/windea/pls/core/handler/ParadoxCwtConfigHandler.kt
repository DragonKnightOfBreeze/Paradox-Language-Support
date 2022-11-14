package icu.windea.pls.core.handler

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

object ParadoxCwtConfigHandler {
	@JvmStatic
	fun resolveConfig(element: ParadoxScriptExpressionElement): CwtDataConfig<*>? {
		return when {
			element is ParadoxScriptPropertyKey -> resolvePropertyConfig(element)
			element is ParadoxScriptValue -> resolveValueConfig(element)
			else -> null
		}
	}
	
	@JvmStatic
	fun resolvePropertyConfig(element: ParadoxScriptProperty, allowDefinitionSelf: Boolean = false, hasDefault: Boolean = true): CwtPropertyConfig? {
		return resolveConfig(element, CwtPropertyConfig::class.java, allowDefinitionSelf, hasDefault)
	}
	
	@JvmStatic
	fun resolvePropertyConfig(element: ParadoxScriptPropertyKey, allowDefinitionSelf: Boolean = false, hasDefault: Boolean = true): CwtPropertyConfig? {
		return resolveConfig(element, CwtPropertyConfig::class.java, allowDefinitionSelf, hasDefault)
	}
	
	@JvmStatic
	fun resolveValueConfig(element: ParadoxScriptValue, allowDefinitionSelf: Boolean = true, hasDefault: Boolean = true): CwtValueConfig? {
		return resolveConfig(element, CwtValueConfig::class.java, allowDefinitionSelf, hasDefault)
	}
	
	fun <T : CwtConfig<*>> resolveConfig(element: PsiElement, configType: Class<T>, allowDefinitionSelf: Boolean, hasDefault: Boolean, matchType: Int = CwtConfigMatchType.ALL): T? {
		return resolveConfigs(element, configType, allowDefinitionSelf, hasDefault, matchType).firstOrNull()
	}
	
	@Suppress("UNCHECKED_CAST")
	@JvmStatic
	fun <T : CwtConfig<*>> resolveConfigs(element: PsiElement, configType: Class<T>, allowDefinitionSelf: Boolean, hasDefault: Boolean, matchType: Int = CwtConfigMatchType.ALL): List<T> {
		//当输入的元素是key或property时，输入的规则类型必须是property
		return when(configType) {
			CwtPropertyConfig::class.java -> {
				val valueElement = when {
					element is ParadoxScriptProperty -> element.propertyValue?.value
					element is ParadoxScriptPropertyKey -> element.propertyValue?.value
					else -> throw UnsupportedOperationException()
				}
				val definitionElementInfo = ParadoxDefinitionElementInfoHandler.get(element) ?: return emptyList()
				if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return emptyList()
				//如果无法匹配value，则取第一个
				val configs = definitionElementInfo.getConfigs(matchType)
				val configGroup = definitionElementInfo.configGroup
				buildList {
					for(config in configs) {
						val propertyConfig = config as? CwtPropertyConfig ?: continue
						//不完整的属性 - 不匹配值
						if(valueElement == null) {
							add(propertyConfig)
							continue
						}
						val valueExpression = propertyConfig.valueExpression
						val expression = ParadoxScriptExpression.resolve(valueElement)
						if(CwtConfigHandler.matchesScriptExpression(expression, valueExpression, configGroup, matchType)) {
							add(propertyConfig)
						}
					}
					if(hasDefault && isEmpty()) {
						configs.firstOrNull()?.let { add(it) }
					}
				} as List<T>
			}
			CwtValueConfig::class.java -> {
				val valueElement = when {
					element is ParadoxScriptValue -> element
					else -> throw UnsupportedOperationException()
				}
				val parent = element.parent
				when(parent) {
					//如果value是property的value
					is ParadoxScriptPropertyValue -> {
						val property = parent.parent as? ParadoxScriptProperty ?: return emptyList()
						val definitionElementInfo = property.definitionElementInfo ?: return emptyList()
						if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return emptyList()
						val configs = definitionElementInfo.getConfigs(matchType)
						val configGroup = definitionElementInfo.configGroup
						buildList {
							for(config in configs) {
								val propertyConfig = config as? CwtPropertyConfig ?: continue
								val expression = ParadoxScriptExpression.resolve(valueElement)
								val valueExpression = propertyConfig.valueExpression
								if(CwtConfigHandler.matchesScriptExpression(expression, valueExpression, configGroup, matchType)) {
									add(propertyConfig.valueConfig)
								}
							}
							if(hasDefault && isEmpty()) {
								configs.findIsInstance<CwtPropertyConfig>()?.valueConfig?.let { add(it) }
							}
						} as List<T>
					}
					//如果value是block中的value
					is ParadoxScriptBlock -> {
						val property = parent.parent?.parent as? ParadoxScriptProperty ?: return emptyList()
						val definitionElementInfo = property.definitionElementInfo ?: return emptyList()
						val childValueConfigs = definitionElementInfo.getChildValueConfigs()
						if(childValueConfigs.isEmpty()) return emptyList()
						val configGroup = definitionElementInfo.configGroup
						buildList {
							for(childValueConfig in childValueConfigs) {
								val expression = ParadoxScriptExpression.resolve(element)
								val valueExpression = childValueConfig.valueExpression
								if(CwtConfigHandler.matchesScriptExpression(expression, valueExpression, configGroup, matchType)) {
									add(childValueConfig)
								}
								if(hasDefault && isEmpty()) {
									childValueConfigs.singleOrNull()?.let { add(it) }
								}
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