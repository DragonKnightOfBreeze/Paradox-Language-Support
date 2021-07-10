package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.type.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 8 * 1024
	
	fun exists(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		val (type, subtype) = resolveTypeExpression(typeExpression)
		return existsElement(type, project, scope) { element -> matches(element, name, subtype) }
	}
	
	fun findOne(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val (type, subtype) = resolveTypeExpression(typeExpression)
		
		if(preferFirst) {
			return findFirstElement(type, project, scope) { element -> matches(element, name, subtype) }
		} else {
			return findLastElement(type, project, scope) { element -> matches(element, name, subtype) }
		}
	}
	
	fun findAll(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val (type, subtype) = resolveTypeExpression(typeExpression)
		return findAllElements(type, project, scope) { element -> matches(element, name, subtype) }
	}
	
	fun findAll(typeExpression: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val (type, subtype) = resolveTypeExpression(typeExpression)
		return findAllElements(type, project, scope) { element -> matches(element, subtype) }
	}
	
	fun findAllByKeyword(keyword: String, typeExpression: String, project: Project, scope: GlobalSearchScope, maxSize: Int): List<ParadoxScriptProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		//需要保证返回结果的名字的唯一性
		val (type, subtype) = resolveTypeExpression(typeExpression)
		val names = mutableSetOf<String>()
		return findAllElements(type, project, scope, maxSize = maxSize) { element -> matchesAndDistinct(element, keyword, subtype, names) }
	}
	
	private fun matches(element: ParadoxScriptProperty, name: String, subtype: String?): Boolean {
		if(subtype == null) {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			return definitionInfo.name == name
		} else {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			return definitionInfo.name == name && subtype in definitionInfo.subtypes
		}
	}
	
	private fun matches(element: ParadoxScriptProperty, subtype: String?): Boolean {
		if(subtype == null) {
			return true
		} else {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			return subtype in definitionInfo.subtypes
		}
	}
	
	private fun matchesAndDistinct(element: ParadoxScriptProperty, keyword: String, subtype: String?, names: MutableSet<String>): Boolean {
		if(subtype == null) {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			val name = definitionInfo.name
			if(!names.add(name)) return false
			return name.matchesKeyword(keyword)
		} else {
			val definitionInfo = element.paradoxDefinitionInfo ?: return false
			val name = definitionInfo.name
			if(!names.add(name)) return false
			return name.matchesKeyword(keyword) && subtype in definitionInfo.subtypes
		}
	}
}

