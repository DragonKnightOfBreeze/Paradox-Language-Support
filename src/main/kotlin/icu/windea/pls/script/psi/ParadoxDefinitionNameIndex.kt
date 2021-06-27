package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 8 * 1024
	
	fun exists(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		if(typeExpression == null) return existsElement(name, project, scope)
		val (type, subtype) = resolveTypeExpression(typeExpression)
		return existsElement(name, project, scope) { element -> matches(element, type, subtype) }
	}
	
	fun findOne(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		if(preferFirst) {
			if(typeExpression == null) return findFirstElement(name, project, scope)
			val (type, subtype) = resolveTypeExpression(typeExpression)
			return findFirstElement(name, project, scope) { element -> matches(element, type, subtype) }
		} else {
			if(typeExpression == null) return findLastElement(name, project, scope)
			val (type, subtype) = resolveTypeExpression(typeExpression)
			return findLastElement(name, project, scope) { element -> matches(element, type, subtype) }
		}
	}
	
	fun findAll(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		if(typeExpression == null) return findAllElements(name, project, scope)
		val (type, subtype) = resolveTypeExpression(typeExpression)
		return findAllElements(name, project, scope) { element -> matches(element, type, subtype) }
	}
	
	fun findAll(typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		if(typeExpression == null) return findAllElementsByKeys(project, scope)
		val (type, subtype) = resolveTypeExpression(typeExpression)
		return findAllElementsByKeys(project, scope) { element -> matches(element, type, subtype) }
	}
	
	private fun matches(element: ParadoxScriptProperty, type: String, subtype: String?): Boolean {
		if(subtype == null) {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			return type == definitionInfo.type
		} else {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			return type == definitionInfo.type && subtype in definitionInfo.subtypes
		}
	}
}
