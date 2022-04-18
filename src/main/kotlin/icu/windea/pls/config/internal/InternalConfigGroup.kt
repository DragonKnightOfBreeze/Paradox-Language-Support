package icu.windea.pls.config.internal

import icu.windea.pls.*

class InternalConfigGroup(
	configMap: InternalConfigMap
) {
	val locales: Array<ParadoxLocaleConfig>
	val localeMap: Map<String, ParadoxLocaleConfig>
	val sequentialNumbers: Array<ParadoxSequentialNumberConfig>
	val sequentialNumberMap: Map<String, ParadoxSequentialNumberConfig>
	val colors: Array<ParadoxColorConfig>
	val colorMap: Map<String, ParadoxColorConfig>
	
	init {
		//初始化locale数据
		locales = configMap.getValue("locale").mapToArray {
			val id = it.getValue("id") as String
			val description = it.getValue("description") as String
			ParadoxLocaleConfig(id, description)
		}
		localeMap = locales.associateBy { it.id }
		
		//初始化sequentialNumber数据
		sequentialNumbers = configMap.getValue("sequentialNumber").mapToArray {
			val id = it.getValue("id") as String
			val description = it.getValue("description") as String
			val placeholderText = it.getValue("placeholderText") as String
			ParadoxSequentialNumberConfig(id, description, placeholderText)
		}
		sequentialNumberMap = sequentialNumbers.associateBy { it.id }
		
		//初始化color数据
		colors = configMap.getValue("color").mapToArray {
			val id = it.getValue("id") as String
			val description = it.getValue("description") as String
			val colorRgb = it.getValue("colorRgb") as Int
			val colorText = it.getValue("colorText") as String
			ParadoxColorConfig(id, description, colorRgb, colorText)
		}
		colorMap = colors.associateBy { it.id }
	}
}
