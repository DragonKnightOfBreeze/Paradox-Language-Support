package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.type.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 4 * 1024
	
	fun getOne(name: String, type: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return null
		return if(preferFirst) elements.firstOrNull { element -> matchesName(element, name) }
		else elements.lastOrNull { element -> matchesName(element, name) }
	}
	
	fun getAll(name: String, type: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(element in elements) {
			if(matchesName(element, name)) result.add(element)
		}
		return result
	}
	
	fun getAll(type: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(element in elements) {
			result.add(element)
		}
		return result
	}
	
	inline fun filter(type: String, project: Project, scope: GlobalSearchScope, predicate: (String) -> Boolean): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(element in elements) {
			val name = element.paradoxDefinitionInfo?.name
			if(name != null && predicate(name)) result.add(element)
		}
		return result
	}
	
	@PublishedApi
	internal fun matchesName(element: ParadoxScriptProperty, name: String): Boolean {
		return element.paradoxDefinitionInfo?.name == name
	}
}

