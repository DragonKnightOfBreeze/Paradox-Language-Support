package icu.windea.pls.config.definition

import com.intellij.openapi.project.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

object TextColorConfigHandler {
	/**
	 * 得到textcolor的对应颜色配置。
	 */
	@JvmStatic
	fun getTextColorConfigs(gameType: ParadoxGameType, project: Project, context: Any? = null): List<ParadoxTextColorConfig> {
		val selector = definitionSelector().gameType(gameType).preferRootFrom(context).distinctByName()
		val definitions = ParadoxDefinitionSearch.search("textcolor", project, selector = selector).findAll()
		if(definitions.isEmpty()) return emptyList()
		return definitions.mapNotNull { doGetTextColorConfig(it, it.name, gameType) } //it.name == it.definitionInfo.name
	}
	
	fun getTextColorConfig(name: String, gameType: ParadoxGameType, project: Project, context: Any? = null): ParadoxTextColorConfig? {
		val selector = definitionSelector().gameType(gameType).preferRootFrom(context)
		val definition = ParadoxDefinitionSearch.search(name, "textcolor", project, selector = selector).find() ?: return null
		return doGetTextColorConfig(definition, name, gameType)
	}
	
	private fun doGetTextColorConfig(definition: ParadoxScriptDefinitionElement, name: String, gameType: ParadoxGameType): ParadoxTextColorConfig? {
		//要求输入的name必须是单个字母或数字
		if(name.singleOrNull()?.let { it.isExactLetter() || it.isExactDigit() } != true) return null
		if(definition !is ParadoxScriptProperty) return null
		return definition.getOrPutUserData(PlsKeys.textColorConfigKey) {
			val rgbList = definition.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
			return ParadoxTextColorConfig(name, gameType, definition.createPointer(), rgbList[0], rgbList[1], rgbList[2])
		}
	}
}
