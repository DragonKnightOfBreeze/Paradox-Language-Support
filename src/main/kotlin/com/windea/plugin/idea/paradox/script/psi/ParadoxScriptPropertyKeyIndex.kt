package com.windea.plugin.idea.paradox.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.*

object ParadoxScriptPropertyKeyIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradoxScript.property.index")
	
	override fun getKey() = key
	
	fun getOne(name: String,type:String? = null, project: Project, scope: GlobalSearchScope): ParadoxScriptProperty? {
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			if(type == null || type == element.paradoxDefinitionInfo?.type) return element
		}
		return null
	}
	
	fun getAll(name: String,type:String? = null, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			if(type == null || type == element.paradoxDefinitionInfo?.type) result.add(element)
		}
		return result
	}
	
	fun getAll(type:String? = null,project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val keys = getAllKeys(project)
		for(key in keys) {
			for(element in get(key, project, scope)) {
				if(type == null || type == element.paradoxDefinitionInfo?.type) result.add(element)
			}
		}
		return result
	}
	
	inline fun filter(type:String? = null,project: Project, scope: GlobalSearchScope, predicate:(String)->Boolean): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val keys = getAllKeys(project)
		for(key in keys) {
			if(predicate(key)) {
				for(element in get(key, project, scope)) {
					if(type == null || type == element.paradoxDefinitionInfo?.type)  result.add(element)
				}
			}
		}
		return result
	}
}
