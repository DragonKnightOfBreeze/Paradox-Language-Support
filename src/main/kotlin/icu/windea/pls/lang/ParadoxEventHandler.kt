package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

object ParadoxEventHandler {
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
	
	@JvmStatic
	fun getEvents(project: Project, context: Any?): Set<ParadoxScriptProperty> {
		val selector = definitionSelector(project, context).contextSensitive().distinctByName()
		val technologies = mutableSetOf<ParadoxScriptProperty>()
		ParadoxDefinitionSearch.search("event", selector).processQuery {
			if(it is ParadoxScriptProperty) technologies.add(it)
			true
		}
		return technologies
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptProperty): String {
		return element.name // = element.definitionInfo.name
	}
	
	@JvmStatic
	fun getNamespace(element: ParadoxScriptProperty): String {
		return getName(element).substringBefore(".") //enough
	}
	
	/**
	 * 得到event的需要匹配的namespace。
	 */
	@JvmStatic
	fun getMatchedNamespace(event: ParadoxScriptProperty): ParadoxScriptProperty? {
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
	
	@JvmStatic
	fun getLocalizedName(definition: ParadoxScriptProperty): ParadoxLocalisationProperty? {
		return definition.definitionInfo?.resolvePrimaryLocalisation(definition)
	}
	
	@JvmStatic
	fun getIconFile(definition: ParadoxScriptProperty): PsiFile? {
		return definition.definitionInfo?.resolvePrimaryImage(definition)
	}
	
	/**
	 * 得到指定事件可能调用的所有事件。
	 */
	@JvmStatic
	fun getInvocations(definition: ParadoxScriptProperty): Set<String> {
		return emptySet() //TODO
	}
}