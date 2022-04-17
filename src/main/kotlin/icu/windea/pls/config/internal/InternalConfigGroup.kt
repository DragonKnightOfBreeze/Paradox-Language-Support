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
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			ParadoxLocaleConfig(name, description)
		}
		localeMap = locales.associateBy { it.name }
		
		//初始化sequentialNumber数据
		sequentialNumbers = configMap.getValue("sequentialNumber").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val placeholderText = it.getValue("placeholderText") as String
			ParadoxSequentialNumberConfig(name, description, placeholderText)
		}
		sequentialNumberMap = sequentialNumbers.associateBy { it.name }
		
		//初始化color数据
		colors = configMap.getValue("color").mapToArray {
			val name = it.getValue("name") as String
			val description = it.getValue("description") as String
			val colorRgb = it.getValue("colorRgb") as Int
			val colorText = it.getValue("colorText") as String
			ParadoxColorConfig(name, description, colorRgb, colorText)
		}
		colorMap = colors.associateBy { it.name }
	}
}
