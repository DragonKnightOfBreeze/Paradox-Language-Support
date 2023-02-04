package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxTextColorHandler {
	/**
	 * 得到textcolor的对应颜色配置。
	 */
	@JvmStatic
	fun getInfos(gameType: ParadoxGameType, project: Project, context: Any? = null): List<ParadoxTextColorInfo> {
		val selector = definitionSelector().gameType(gameType).preferRootFrom(context).distinctByName()
		val definitions = ParadoxDefinitionSearch.search("textcolor", project, selector = selector).findAll()
		if(definitions.isEmpty()) return emptyList()
		return definitions.mapNotNull { definition -> getInfoFromCache(definition) } //it.name == it.definitionInfo.name
	}
	
	@JvmStatic
	fun getInfo(name: String, gameType: ParadoxGameType, project: Project, context: Any? = null): ParadoxTextColorInfo? {
		val selector = definitionSelector().gameType(gameType).preferRootFrom(context)
		val definition = ParadoxDefinitionSearch.search(name, "textcolor", project, selector = selector).find() 
		if(definition == null) return null
		return getInfoFromCache(definition)
	}
	
	private fun getInfoFromCache(definition: ParadoxScriptDefinitionElement): ParadoxTextColorInfo? {
		if(definition !is ParadoxScriptProperty) return null
		return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedTextColorInfoKey) {
			val value = resolveInfo(definition)
			CachedValueProvider.Result.create(value, definition)
		}
	}
	
	private fun resolveInfo(definition: ParadoxScriptProperty): ParadoxTextColorInfo? {
		//要求输入的name必须是单个字母或数字
		val name = definition.name
		if(name.singleOrNull()?.let { it.isExactLetter() || it.isExactDigit() } != true) return null
		val gameType = selectGameType(definition) ?: return null
		val rgbList = definition.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
		val value = ParadoxTextColorInfo(name, gameType, definition.createPointer(), rgbList[0], rgbList[1], rgbList[2])
		return value
	}
}
