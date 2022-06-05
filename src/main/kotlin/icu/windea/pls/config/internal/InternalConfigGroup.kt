package icu.windea.pls.config.internal

import com.intellij.util.containers.*
import icu.windea.pls.*
import icu.windea.pls.annotation.*
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
	val localeFlagMap: Map<String, ParadoxLocaleConfig>
	val colors: Array<ParadoxColorConfig>
	val colorMap: Map<String, ParadoxColorConfig>
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
		var colorsConfig: CwtPropertyConfig? = null
		var predefinedVariableConfig: CwtPropertyConfig? = null
		for(prop in localisationConfig.properties) {
			when(prop.key) {
				"locales" -> localesConfig = prop
				"colors" -> colorsConfig = prop
				"predefined_variables" -> predefinedVariableConfig = prop
			}
		}
		
		//初始化locale数据
		locales = localesConfig!!.properties!!.mapToArray {
			val id = it.key
			val description = it.documentation.orEmpty()
			val languageTag = it.properties?.find { p -> p.key == "language_tag" }?.stringValue!!
			ParadoxLocaleConfig(id, description, languageTag, it.pointer)
		}
		localeMap = locales.associateBy { it.id }
		localeFlagMap = locales.associateBy { it.languageTag }
		
		//初始化color数据
		colors = colorsConfig!!.properties!!.mapToArray { prop ->
			val id = prop.key
			val description = prop.documentation.orEmpty()
			val rgbList = prop.values?.mapNotNull { it.intValue }
			if(rgbList == null || rgbList.size != 3) throw InternalError()
			ParadoxColorConfig(id, description, prop.pointer, rgbList[0], rgbList[1], rgbList[2])
		}
		colorMap = colors.associateBy { it.id }
		
		//初始化predefinedVariable数据
		predefinedVariables = predefinedVariableConfig!!.properties!!.mapToArray { prop ->
			val id = prop.key
			val description = prop.documentation.orEmpty()
			ParadoxPredefinedVariableConfig(id, description, prop.pointer)
		}
		predefinedVariableMap = predefinedVariables.associateBy { it.id }
	}
}
