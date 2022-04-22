package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.name.index")
	private const val cacheSize = 4 * 1024
	
	override fun getKey() = key
	
	override fun getCacheSize() = cacheSize
	
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
		val stub = element.stub
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetType = runCatching { stub?.type }.getOrNull() ?: definitionInfo?.type ?: return false
		if(type != targetType) return false
		if(subtype != null) {
			val targetSubtypes = runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
}
