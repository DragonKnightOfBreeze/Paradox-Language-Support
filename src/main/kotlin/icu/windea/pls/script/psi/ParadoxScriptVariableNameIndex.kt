package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*

object ParadoxScriptVariableNameIndex: StringStubIndexExtension<ParadoxScriptVariable>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptVariable>("paradox.scriptVariable.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 1024
	
	fun getOne(name: String, project: Project, scope: GlobalSearchScope,preferFirst:Boolean): ParadoxScriptVariable? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptVariable::class.java)
		return if(preferFirst) elements.firstOrNull() else elements.lastOrNull()
	}
	
	fun getAll(name: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptVariable> {
		val result =  mutableListOf<ParadoxScriptVariable>()
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptVariable::class.java)
		for(element in elements) {
			result.add(element)
		}
		return result
	}
	
	fun getAll(project: Project, scope: GlobalSearchScope): List<ParadoxScriptVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxScriptVariable>()
		val keys = getAllKeys(project)
		for(key in keys) {
			for(element in get(key, project, scope)) {
				result.add(element)
			}
		}
		return result
	}
	
	inline fun filter(project: Project, scope: GlobalSearchScope, predicate:(String)->Boolean): List<ParadoxScriptVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxScriptVariable>()
		val keys = getAllKeys(project)
		for(key in keys) {
			if(predicate(key)) {
				for(element in get(key, project, scope)) {
					result.add(element)
				}
			}
		}
		return result
	}
}

