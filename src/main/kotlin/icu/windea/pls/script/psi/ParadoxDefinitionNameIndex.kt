package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import org.apache.tools.ant.taskdefs.*

object ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 4 * 1024
	
	fun getOne(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return null
		return if(preferFirst) elements.firstOrNull { element -> matchesTypeExpression(element, typeExpression) }
		else elements.lastOrNull { element -> matchesTypeExpression (element, typeExpression) }
	}
	
	fun getAll(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(element in elements) {
			if(matchesTypeExpression(element, typeExpression)) result.add(element)
		}
		return result
	}
	
	fun getAll(typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(key in keys) {
			val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxScriptProperty::class.java)
			for(element in elements) {
				if(matchesTypeExpression(element, typeExpression)) result.add(element)
			}
		}
		return result
	}
	
	inline fun filter(typeExpression: String?, project: Project, scope: GlobalSearchScope, predicate: (String) -> Boolean): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(key in keys) {
			if(predicate(key)) {
				val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxScriptProperty::class.java)
				for(element in elements) {
					if(matchesTypeExpression(element, typeExpression)) result.add(element)
				}
			}
		}
		return result
	}
	
	@PublishedApi
	internal fun matchesTypeExpression(element: ParadoxScriptProperty, type: String?): Boolean {
		return type == null || element.paradoxDefinitionInfo?.matchesTypeExpression(type) == true
	}
}
