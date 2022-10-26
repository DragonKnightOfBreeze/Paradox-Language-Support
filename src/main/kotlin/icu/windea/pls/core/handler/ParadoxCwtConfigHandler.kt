package icu.windea.pls.core.handler

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.psi.*

object ParadoxCwtConfigHandler {
	@JvmStatic
	fun resolveConfig(element: ParadoxScriptExpressionElement): CwtKvConfig<*>? {
		return when {
			element is ParadoxScriptPropertyKey -> resolvePropertyConfig(element)
			element is ParadoxScriptValue -> resolveValueConfig(element as ParadoxScriptValue)
			else -> null
		}
	}
	
	@JvmStatic
	fun resolvePropertyConfig(element: ParadoxScriptProperty, allowDefinitionSelf: Boolean = false, hasDefault: Boolean = true): CwtPropertyConfig? {
		return resolveConfigs(element, CwtPropertyConfig::class.java, allowDefinitionSelf, hasDefault).firstOrNull()
	}
	
	@JvmStatic
	fun resolvePropertyConfig(element: ParadoxScriptPropertyKey, allowDefinitionSelf: Boolean = false, hasDefault: Boolean = true): CwtPropertyConfig? {
		return resolveConfigs(element, CwtPropertyConfig::class.java, allowDefinitionSelf, hasDefault).firstOrNull()
	}
	
	@JvmStatic
	fun resolveValueConfig(element: ParadoxScriptValue, allowDefinitionSelf: Boolean = true, hasDefault: Boolean = true): CwtValueConfig? {
		return resolveConfigs(element, CwtValueConfig::class.java, allowDefinitionSelf, hasDefault).firstOrNull()
	}
	
	@Suppress("UNCHECKED_CAST")
	@JvmStatic
	fun <T : CwtConfig<*>> resolveConfigs(
		element: PsiElement,
		configType: Class<T>,
		allowDefinitionSelf: Boolean = true,
		hasDefault: Boolean = true,
		valueExpressionPredicate: ((CwtValueExpression) -> Boolean)? = null
	): List<T> {
		//当输入的元素是key或property时，输入的规则类型必须是property
		return when(configType) {
			CwtPropertyConfig::class.java -> {
				val valueElement = when {
					element is ParadoxScriptProperty -> element.propertyValue?.value
					element is ParadoxScriptPropertyKey -> element.propertyValue?.value
					else -> return emptyList()
				}
				val definitionElementInfo = ParadoxDefinitionElementInfoHandler.get(element) ?: return emptyList()
				if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return emptyList()
				//如果无法匹配value，则取第一个
				val propertyConfigs = definitionElementInfo.propertyConfigs
				val configGroup = definitionElementInfo.configGroup
				buildList {
					for(propertyConfig in propertyConfigs) {
						//不完整的属性 - 不匹配值
						if(valueElement == null) {
							add(propertyConfig)
							continue
						}
						val valueExpression = propertyConfig.valueExpression
						if(valueExpressionPredicate != null && !valueExpressionPredicate(valueExpression)) continue
						if(CwtConfigHandler.matchesValue(valueExpression, valueElement, configGroup)) {
							add(propertyConfig)
						}
					}
					if(hasDefault && isEmpty()) {
						propertyConfigs.firstOrNull()?.let { add(it) }
					}
				} as List<T>
			}
			CwtValueConfig::class.java -> {
				val valueElement = element as? ParadoxScriptValue ?: return emptyList()
				val parent = element.parent
				when(parent) {
					//如果value是property的value
					is ParadoxScriptPropertyValue -> {
						val property = parent.parent as? ParadoxScriptProperty ?: return emptyList()
						val definitionElementInfo = property.definitionElementInfo ?: return emptyList()
						if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return emptyList()
						val propertyConfigs = definitionElementInfo.propertyConfigs
						val configGroup = definitionElementInfo.configGroup
						buildList {
							for(propertyConfig in propertyConfigs) {
								val valueExpression = propertyConfig.valueExpression
								if(valueExpressionPredicate != null && !valueExpressionPredicate(valueExpression)) continue
								if(CwtConfigHandler.matchesValue(valueExpression, valueElement, configGroup)){
									add(propertyConfig.valueConfig)
								}
							}
							if(hasDefault && isEmpty()) {
								propertyConfigs.singleOrNull()?.valueConfig?.let { add(it) }
							}
						} as List<T>
					}
					//如果value是block中的value
					is ParadoxScriptBlock -> {
						val property = parent.parent?.parent as? ParadoxScriptProperty ?: return emptyList()
						val definitionElementInfo = property.definitionElementInfo ?: return emptyList()
						val childValueConfigs = definitionElementInfo.childValueConfigs
						if(childValueConfigs.isEmpty()) return emptyList()
						val configGroup = definitionElementInfo.configGroup
						buildList {
							for(childValueConfig in childValueConfigs) {
								val valueExpression = childValueConfig.valueExpression
								if(valueExpressionPredicate != null && !valueExpressionPredicate(valueExpression)) continue
								if(CwtConfigHandler.matchesValue(valueExpression, element, configGroup)){
									add(childValueConfig)
								}
								if(hasDefault && isEmpty()){
									childValueConfigs.singleOrNull()?.let { add(it) }
								}
							}
						} as List<T>
					}
					else -> return emptyList()
				}
			}
			else -> emptyList()
		}
	}
}