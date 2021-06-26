package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 4 * 1024
	
	fun exists(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		if(typeExpression == null) return name in getAllKeys(project)
		val (type,subtype) = resolveTypeExpression(typeExpression)
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxScriptProperty::class.java)
		return elements.any { element -> matches(element, type,subtype) }
	}
	
	fun getOne(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return null
		if(typeExpression == null) return if(preferFirst) elements.first() else elements.last()
		val (type,subtype) = resolveTypeExpression(typeExpression)
		return if(preferFirst) elements.firstOrNull { element -> matches(element, type,subtype) }
		else elements.lastOrNull { element -> matches(element, type,subtype) }
	}
	
	fun getAll(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		if(typeExpression == null) return elements.toList()
		val (type,subtype) = resolveTypeExpression(typeExpression)
		return elements.filter { element -> matches(element,type,subtype) }
	}
	
	fun getAll(typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		if(typeExpression == null) return keys.flatMap { key ->
			StubIndex.getElements(getKey(), key, project, scope, ParadoxScriptProperty::class.java)
		}
		val (type,subtype) = resolveTypeExpression(typeExpression)
		val result = mutableListOf<ParadoxScriptProperty>()
		for(key in keys) {
			val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxScriptProperty::class.java)
			for(element in elements) {
				if(matches(element, type,subtype)) result.add(element)
			}
		}
		return result
	}
	
	private fun matches(element: ParadoxScriptProperty, type: String, subtype: String?): Boolean {
		val definitionInfo = element.paradoxDefinitionInfo ?: return false
		return type == definitionInfo.type && (subtype == null || subtype in definitionInfo.subtypes)
	}
}
