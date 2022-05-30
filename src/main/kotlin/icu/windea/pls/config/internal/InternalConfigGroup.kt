package icu.windea.pls.config.internal

import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.cwt.psi.CwtInt
import icu.windea.pls.script.psi.*

class InternalConfigGroup(
	configMap: InternalConfigMap
) {
	val locales: Array<ParadoxLocaleConfig>
	val localeMap: Map<String, ParadoxLocaleConfig>
	val localeFlagMap: Map<String, ParadoxLocaleConfig>
	val colors: Array<ParadoxColorConfig>
	val colorMap: Map<String, ParadoxColorConfig>
	
	init {
		//初始化locale数据
		val declarationsConfig = configMap.getValue("declarations.cwt")
		var localesConfig: CwtPropertyConfig? = null
		var colorsConfig: CwtPropertyConfig ? = null
		for(prop in declarationsConfig.properties) {
			when(prop.key){
				"locales" -> localesConfig = prop
				"colors" -> colorsConfig = prop
			}
		}
		
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
			ParadoxColorConfig(id, description, rgbList[0], rgbList[1], rgbList[2], prop.pointer)
		}
		colorMap = colors.associateBy { it.id }
	}
}
