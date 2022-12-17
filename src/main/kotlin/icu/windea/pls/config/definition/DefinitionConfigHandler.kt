package icu.windea.pls.config.definition

import com.intellij.openapi.project.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

object DefinitionConfigHandler {
	//region event
	@JvmStatic
	fun isValidEventNamespace(eventNamespace: String): Boolean {
		if(eventNamespace.isEmpty()) return false
		return eventNamespace.all { it.isExactLetter() || it =='_' }
	}
	
	@JvmStatic
	fun isValidEventId(eventId: String, eventNamespace: String): Boolean {
		if(eventId.isEmpty()) return false
		//格式：{namespace}.{0}，其中：{namespace}必须匹配事件命名空间，{0}必须是非负整数且允许作为前缀的0
		val dotIndex = eventId.indexOf('.') //a.1 1 0,1 2,3 
		if(dotIndex == -1) return false
		val prefix = eventId.substring(0, dotIndex)
		if(!prefix.equals(eventNamespace, true)) return false //TODO 不确定，应当需要忽略带小写
		val no = eventId.substring(dotIndex + 1)
		return no.isNotEmpty() && no.all { it.isExactDigit() }
	}
	
	/**
	 * 得到event的需要匹配的namespace。
	 */
	@JvmStatic
	fun getEventNamespace(event: ParadoxScriptProperty): ParadoxScriptProperty? {
		var current = event.prevSibling ?: return null
		while(true) {
			if(current is ParadoxScriptProperty && current.name.equals("namespace", true)) {
				if(current.propertyValue is ParadoxScriptString) {
					return current
				} else {
					return null //invalid
				}
			}
			current = current.prevSibling ?: return null
		}
	}
	//endregion

	//region textcolor
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
	
	private fun doGetTextColorConfig(definition: ParadoxDefinitionProperty, name: String, gameType: ParadoxGameType): ParadoxTextColorConfig? {
		//要求输入的name必须是单个字母或数字
		if(name.singleOrNull()?.let { it.isExactLetter() || it.isExactDigit() } != true) return null
		if(definition !is ParadoxScriptProperty) return null
		return definition.getOrPutUserData(PlsKeys.textColorConfigKey) {
			val rgbList = definition.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
			return ParadoxTextColorConfig(name, gameType, definition.createPointer(), rgbList[0], rgbList[1], rgbList[2])
		}
	}
	//endregion
}
