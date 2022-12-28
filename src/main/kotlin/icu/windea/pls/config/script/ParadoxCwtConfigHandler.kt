package icu.windea.pls.config.script

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
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
					else -> null
				}
				val definitionMemberInfo = memberElement.definitionMemberInfo ?: return emptyList()
				if(!allowDefinitionSelf && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
				//如果无法匹配value，则取第一个
				val configs = definitionMemberInfo.getConfigs(matchType)
				val configGroup = definitionMemberInfo.configGroup
				buildList {
					for(config in configs) {
						val propertyConfig = config as? CwtPropertyConfig ?: continue
						//不完整的属性 - 不匹配值
						if(expression == null) {
							add(propertyConfig)
							continue
						}
						val valueExpression = propertyConfig.valueExpression
						if(CwtConfigHandler.matchesScriptExpression(expression, valueExpression, configGroup, matchType)) {
							add(propertyConfig)
						}
					}
					if(orDefault && isEmpty()) {
						configs.forEach { add(it) }
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
							for(config in configs) {
								val propertyConfig = config as? CwtPropertyConfig ?: continue
								val valueExpression = propertyConfig.valueExpression
								if(CwtConfigHandler.matchesScriptExpression(expression, valueExpression, configGroup, matchType)) {
									add(propertyConfig.valueConfig)
								}
							}
							if(orDefault && isEmpty()) {
								configs.filterIsInstance<CwtPropertyConfig>().forEach { add(it.valueConfig) }
							}
						} as List<T>
					}
					//如果value是blockElement中的value
					is ParadoxScriptBlockElement -> {
						val property = parent.parent as? ParadoxScriptDefinitionElement ?: return emptyList()
						val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
						val childValueConfigs = definitionMemberInfo.getChildValueConfigs()
						if(childValueConfigs.isEmpty()) return emptyList()
						val configGroup = definitionMemberInfo.configGroup
						buildList {
							for(childValueConfig in childValueConfigs) {
								val valueExpression = childValueConfig.valueExpression
								if(CwtConfigHandler.matchesScriptExpression(expression, valueExpression, configGroup, matchType)) {
									add(childValueConfig)
								}
								if(orDefault && isEmpty()) {
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
