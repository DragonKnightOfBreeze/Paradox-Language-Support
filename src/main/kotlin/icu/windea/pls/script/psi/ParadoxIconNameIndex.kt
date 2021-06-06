package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*

object ParadoxIconNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.icon.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 1024
	
	fun getOne(name: String, project: Project, scope: GlobalSearchScope,preferFirst:Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptProperty::class.java)
		return if(preferFirst) elements.firstOrNull() else elements.lastOrNull()
	}
	
	fun getAll(name: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxScriptProperty>()
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			result.add(element)
		}
		return result
	}
	
	fun getAll(project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxScriptProperty>()
		val keys = ParadoxScriptLocalisationNameIndex.getAllKeys(project)
		for(key in keys) {
			for(element in ParadoxScriptLocalisationNameIndex.get(key, project, scope)) {
				result.add(element)
			}
		}
		return result
	}
	
	inline fun filter(project: Project, scope: GlobalSearchScope, predicate:(String)->Boolean): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxScriptProperty>()
		val keys = ParadoxDefinitionNameIndex.getAllKeys(project)
		for(key in keys) {
			if(predicate(key)) {
				for(element in ParadoxDefinitionNameIndex.get(key, project, scope)) {
					result.add(element)
				}
			}
		}
		return result
	}
}