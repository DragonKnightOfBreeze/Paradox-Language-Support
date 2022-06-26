package icu.windea.pls.config.internal

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.annotation.*
import icu.windea.pls.config.internal.config.*

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