package com.windea.plugin.idea.paradox.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*

object ParadoxScriptLocNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.scriptedLoc.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 256
	
	fun getOne(name: String, project: Project, scope: GlobalSearchScope): ParadoxScriptProperty? {
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			return element
		}
		return null
	}
	
	fun getAll(name: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			result.add(element)
		}
		return result
	}
	
	fun getAll(project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val keys = getAllKeys(project)
		for(key in keys) {
			for(element in get(key, project, scope)) {
				result.add(element)
			}
		}
		return result
	}
	
	inline fun filter(project: Project, scope: GlobalSearchScope, predicate:(String)->Boolean): List<ParadoxScriptProperty> {
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