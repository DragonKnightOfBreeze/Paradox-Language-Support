package icu.windea.pls.config.internal

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.annotation.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.script.psi.*

/**
 * 内置规则的处理器。
 */
object InternalConfigHandler {
	//region System Scope Extensions
	/**
	 * 从内置规则文件中得到指定ID的系统作用域设置。
	 */
	fun getSystemScope(id: String, project: Project? = null):ParadoxSystemScopeConfig? {
		return getInternalConfig(project).systemScopeMap[id]
	}
	
	/**
	 * 从内置规则文件中得到所有系统作用域设置。
	 */
	fun getSystemScopeMap(project: Project? = null): Map<@CaseInsensitive String, ParadoxSystemScopeConfig>{
		return getInternalConfig(project).systemScopeMap
	}
	
	/**
	 * 从内置规则文件中得到所有系统作用域设置。
	 */
	fun getSystemScopes(project: Project? = null): Array<ParadoxSystemScopeConfig>{
		return getInternalConfig(project).systemScopes
	}
	//endregion
	
	//region Localisation Locale Extensions
	/**
	 * 从内置规则文件中得到指定ID的语言区域设置。
	 */
	fun getLocale(id: String, project: Project? = null): ParadoxLocaleConfig? {
		return getInternalConfig(project).localeMap[id]
	}
	
	/**
	 * 从内置规则文件中得到指定FLAG的语言区域设置。
	 */
	fun getLocaleByFlag(flag: String, project: Project? = null): ParadoxLocaleConfig? {
		return getInternalConfig(project).localeFlagMap[flag]
	}
	
	/**
	 * 从内置规则文件中得到得到所有语言区域设置。
	 */
	fun getLocaleMap(project: Project? = null): Map<String, ParadoxLocaleConfig> {
		return getInternalConfig(project).localeMap
	}
	
	/**
	 * 从内置规则文件中得到得到所有语言区域设置。
	 */
	fun getLocaleFlagMap(project: Project? = null): Map<String, ParadoxLocaleConfig> {
		return getInternalConfig(project).localeFlagMap
	}
	
	/**
	 * 从内置规则文件中得到得到所有语言区域设置。
	 */
	fun getLocales(project: Project? = null): Array<ParadoxLocaleConfig> {
		return getInternalConfig(project).locales
	}
	//endregion
	
	//region Localisation Color Extensions
	//如果可以定位到font.gfx#textcolors中的定义，则基于此，否则基于内置规则文件
	//除了大写A有特殊用途其他字母都能自定义rgb颜色值
	/**
	 * 从`interface/fonts.gfx#bitmapfonts.textcolors`中得到指定ID的游戏目录中的或者自定义的颜色设置。或者从内置规则文件中得到。
	 */
	fun getColor(id: String, project: Project? = null): ParadoxColorConfig? {
		if(project != null) runReadAction { doGetColor(id, project) }?.also { return it }
		return getInternalConfig(project).colorMap[id]
	}
	
	private fun doGetColor(id: String, project: Project): ParadoxColorConfig? {
		if(id.singleOrNull()?.isExactLetter() != true) return null
		val definitions = findDefinitionsByType("textcolors", project)
		if(definitions.isEmpty()) return null
		var color: ParadoxColorConfig? = null
		for(definition in definitions) {
			definition.block?.processProperty { prop ->
				val colorId = prop.name
				if(colorId.singleOrNull()?.isExactLetter() != true) return@processProperty true
				if(colorId != id) return@processProperty true
				val rgbList = prop.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
				if(rgbList.size != 3) return@processProperty true
				val description = getInternalConfig(project).colorMap[colorId]?.description.orEmpty() //来自内置规则文件
				val colorConfig = ParadoxColorConfig(colorId, description, prop.createPointer(), rgbList[0], rgbList[1], rgbList[2])
				color = colorConfig
				true
			}
		}
		return color //需要得到的是重载后的颜色设置
	}
	
	/**
	 * 从`interface/fonts.gfx#bitmapfonts.textcolors`中得到所有游戏目录中的或者自定义的颜色设置。或者从内置规则文件中得到。
	 */
	fun getColorMap(project: Project? = null): Map<String, ParadoxColorConfig> {
		if(project != null) runReadAction { doGetColorMap(project) }.takeIf { it.isNotEmpty() }?.also { return it }
		return getInternalConfig(project).colorMap
	}
	
	private fun doGetColorMap(project: Project): Map<String, ParadoxColorConfig> {
		val definitions = findDefinitionsByType("textcolors", project)
		if(definitions.isEmpty()) return emptyMap()
		val configMap = mutableMapOf<String, ParadoxColorConfig>()
		for(definition in definitions) {
			definition.block?.processProperty { prop ->
				val colorId = prop.name
				if(colorId.singleOrNull()?.isExactLetter() != true) return@processProperty true
				val rgbList = prop.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
				if(rgbList.size != 3) return@processProperty true
				val description = getInternalConfig(project).colorMap[colorId]?.description.orEmpty() //来自内置规则文件
				val colorConfig = ParadoxColorConfig(colorId, description, prop.createPointer(), rgbList[0], rgbList[1], rgbList[2])
				configMap[colorId] = colorConfig
				true
			}
		}
		return configMap
	}
	
	/**
	 * 从`interface/fonts.gfx#bitmapfonts.textcolors`中得到所有游戏目录中的或者自定义的颜色设置。或者从内置规则文件中得到。
	 */
	fun getColors(project: Project? = null): Array<ParadoxColorConfig> {
		if(project != null) runReadAction { doGetColorMap(project) }.takeIf { it.isNotEmpty() }?.also { return it.values.toTypedArray() }
		return getInternalConfig(project).colors
	}
	//endregion
	
	//region Localisation Predefined Variable Extensions
	/**
	 * 从内置规则文件中得到指定ID的预定义变量设置。
	 */
	fun getPredefinedVariable(id: String, project: Project? = null): ParadoxPredefinedVariableConfig? {
		return getInternalConfig(project).predefinedVariableMap[id]
	}
	
	/**
	 * 从内置规则文件中得到得到所有预定义变量设置。
	 */
	fun getPredefinedVariableMap(project: Project? = null): Map<String, ParadoxPredefinedVariableConfig> {
		return getInternalConfig(project).predefinedVariableMap
	}
	
	/**
	 * 从内置规则文件中得到得到所有预定义变量设置。
	 */
	fun getPredefinedVariables(project: Project? = null): Array<ParadoxPredefinedVariableConfig> {
		return getInternalConfig(project).predefinedVariables
	}
	//endregion
}