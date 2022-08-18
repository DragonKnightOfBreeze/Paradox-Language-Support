package icu.windea.pls.config.internal

import com.intellij.util.containers.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.internal.config.*

class InternalConfigGroup(
	configMap: InternalConfigMap
) {
	companion object {
		const val scriptConfigFileName = "script_config.pls.cwt"
		const val localisationConfigFileName = "localisation_config.pls.cwt"
	}
	
	val systemScopes: Array<ParadoxSystemScopeConfig>
	val systemScopeMap: Map<@CaseInsensitive String, ParadoxSystemScopeConfig>
	
	val locales: Array<ParadoxLocaleConfig>
	val localeMap: Map<String, ParadoxLocaleConfig>
	val localeMapNotDefault: Map<String, ParadoxLocaleConfig>
	val localeMapByCode: Map<String, ParadoxLocaleConfig>
	val predefinedVariables: Array<ParadoxPredefinedVariableConfig>
	val predefinedVariableMap: Map<String, ParadoxPredefinedVariableConfig>
	
	init {
		val scriptConfig = configMap.getValue(scriptConfigFileName)
		var systemScopeConfig: CwtPropertyConfig? = null
		for(prop in scriptConfig.properties) {
			when(prop.key) {
				"system_scopes" -> systemScopeConfig = prop
			}
		}
		systemScopes = systemScopeConfig!!.properties!!.mapToArray {
			val id = it.key
			val description = it.documentation.orEmpty()
			ParadoxSystemScopeConfig(id, description, it.pointer)
		}
		systemScopeMap = systemScopes.associateByTo(CollectionFactory.createCaseInsensitiveStringMap(systemScopes.size)) { it.id }
		
		val localisationConfig = configMap.getValue(localisationConfigFileName)
		var localesConfig: CwtPropertyConfig? = null
		var predefinedVariableConfig: CwtPropertyConfig? = null
		for(prop in localisationConfig.properties) {
			when(prop.key) {
				"locales" -> localesConfig = prop
				"predefined_variables" -> predefinedVariableConfig = prop
			}
		}
		
		//初始化locale数据
		locales = localesConfig!!.properties!!.mapToArray {
			val id = it.key
			val description = it.documentation.orEmpty()
			val codes = it.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
			ParadoxLocaleConfig(id, description, codes, it.pointer)
		}
		localeMap = locales.associateBy { it.id }
		localeMapNotDefault = localeMap.filterKeys { it != "l_default" } 
		localeMapByCode = buildMap { locales.forEach { locale -> locale.codes.forEach { code -> put(code, locale) } } }
		
		//初始化predefinedVariable数据
		predefinedVariables = predefinedVariableConfig!!.properties!!.mapToArray { prop ->
			val id = prop.key
			val description = prop.documentation.orEmpty()
			ParadoxPredefinedVariableConfig(id, description, prop.pointer)
		}
		predefinedVariableMap = predefinedVariables.associateBy { it.id }
	}
}
