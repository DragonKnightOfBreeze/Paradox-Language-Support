package icu.windea.pls.config.internal

import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.internal.config.*

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
		//var sequentialNumbersConfig: CwtPropertyConfig ? = null
		var colorsConfig: CwtPropertyConfig ? = null
		for(prop in declarationsConfig.properties) {
			when(prop.key){
				"locales" -> localesConfig = prop
				//"sequential_numbers" -> sequentialNumbersConfig = prop
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
		
		////初始化sequentialNumber数据
		//sequentialNumbers = sequentialNumbersConfig!!.properties!!.mapToArray {
		//	val id = it.key
		//	val description = it.documentation.orEmpty()
		//	val placeholderText = it.properties?.find { p -> p.key == "placeholder_text" }?.stringValue!!
		//	ParadoxSequentialNumberConfig(id, description, placeholderText, it.pointer)
		//}
		//sequentialNumberMap = sequentialNumbers.associateBy { it.id }
		
		//初始化color数据
		colors = colorsConfig!!.properties!!.mapToArray {
			val id = it.key
			val description = it.documentation.orEmpty()
			val colorRgb = it.properties?.find { p -> p.key == "color_rgb" }?.stringValue!!
			ParadoxColorConfig(id, description, colorRgb, it.pointer)
		}
		colorMap = colors.associateBy { it.id }
	}
}
