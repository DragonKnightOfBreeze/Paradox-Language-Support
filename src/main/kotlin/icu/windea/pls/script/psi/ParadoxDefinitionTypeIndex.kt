package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.type.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 4 * 1024
	
	fun exists(name:String,typeExpression: String,project: Project,scope: GlobalSearchScope):Boolean{
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		val (type,subtype) = resolveTypeExpression(typeExpression)
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return false
		return elements.any { element -> matches(element, name,subtype) }
	}
	
	fun getOne(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val (type,subtype) = resolveTypeExpression(typeExpression)
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return null
		return if(preferFirst) elements.firstOrNull { element -> matches(element, name,subtype) }
		else elements.lastOrNull { element -> matches(element, name,subtype) }
	}
	
	fun getAll(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val (type,subtype) = resolveTypeExpression(typeExpression)
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		return elements.filter { element -> matches(element,name,subtype) }
	}
	
	fun getAll(typeExpression: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val (type,subtype) = resolveTypeExpression(typeExpression)
		val elements = StubIndex.getElements(getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		if(elements.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxScriptProperty>()
		for(element in elements) {
			if(matches(element,subtype)) result.add(element)
		}
		return result
	}
	
	private fun matches(element: ParadoxScriptProperty, name: String,subtype:String?): Boolean {
		val definitionInfo = element.paradoxDefinitionInfo ?:return false
		return definitionInfo.name == name && (subtype == null || subtype in definitionInfo.subtypes)
	}
	
	private fun matches(element: ParadoxScriptProperty,subtype:String?): Boolean {
		val definitionInfo = element.paradoxDefinitionInfo ?:return false
		return subtype == null || subtype in definitionInfo.subtypes
	}
}

