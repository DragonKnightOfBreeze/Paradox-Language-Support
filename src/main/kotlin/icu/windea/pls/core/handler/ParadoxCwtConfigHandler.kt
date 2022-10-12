package icu.windea.pls.core.handler

import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
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
	fun resolvePropertyConfig(element: ParadoxScriptProperty, allowDefinitionSelf: Boolean = false, orFirst: Boolean = true): CwtPropertyConfig? {
		val key = element.propertyKey
		return resolvePropertyConfig(key, allowDefinitionSelf, orFirst)
	}
	
	@JvmStatic
	fun resolveValueConfig(element: ParadoxScriptProperty, allowDefinitionSelf: Boolean = true, orSingle: Boolean = true): CwtValueConfig? {
		val value = element.propertyValue?.value ?: return null
		return resolveValueConfig(value, allowDefinitionSelf, orSingle)
	}
	
	@JvmStatic
	fun resolvePropertyConfig(element: ParadoxScriptPropertyKey, allowDefinitionSelf: Boolean = false, orFirst: Boolean = true): CwtPropertyConfig? {
		val definitionElementInfo = element.definitionElementInfo ?: return null
		if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return null
		//如果无法匹配value，则取第一个
		return definitionElementInfo.matchedPropertyConfig
			?: orFirst.ifTrue { definitionElementInfo.propertyConfigs.firstOrNull() }
	}
	
	@JvmStatic
	fun resolveValueConfig(element: ParadoxScriptValue, allowDefinitionSelf: Boolean = true, orSingle: Boolean = true): CwtValueConfig? {
		val parent = element.parent
		when(parent) {
			//如果value是property的value
			is ParadoxScriptPropertyValue -> {
				val property = parent.parent as? ParadoxScriptProperty ?: return null
				val definitionElementInfo = property.definitionElementInfo ?: return null
				if(!allowDefinitionSelf && definitionElementInfo.elementPath.isEmpty()) return null
				//如果无法匹配value，则取唯一的那个
				return definitionElementInfo.matchedPropertyConfig?.valueConfig
					?: orSingle.ifTrue { definitionElementInfo.propertyConfigs.singleOrNull()?.valueConfig }
			}
			//如果value是block中的value
			is ParadoxScriptBlock -> {
				val property = parent.parent?.parent as? ParadoxScriptProperty ?: return null
				val definitionElementInfo = property.definitionElementInfo ?: return null
				val childValueConfigs = definitionElementInfo.childValueConfigs
				if(childValueConfigs.isEmpty()) return null
				val gameType = definitionElementInfo.gameType
				val configGroup = getCwtConfig(element.project).getValue(gameType)
				//如果无法匹配value，则取唯一的那个
				return childValueConfigs.find { CwtConfigHandler.matchesValue(it.valueExpression, element, configGroup) }
					?: orSingle.ifTrue { childValueConfigs.singleOrNull() }
			}
			
			else -> return null
		}
	}
}