package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.type.index")
	private const val cacheSize = 4 * 1024
	
	override fun getKey() = key
	
	override fun getCacheSize() = cacheSize
	
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
		val stub = element.stub
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetName = runCatching { stub?.name }.getOrNull() ?: definitionInfo?.name ?: return false
		if(targetName != name) return false
		if(subtype != null) {
			val targetSubtypes = runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
	
	private fun matches(element: ParadoxScriptProperty, subtype: String?): Boolean {
		val stub = element.stub
		val definitionInfo = if(stub == null) element.definitionInfo else null
		if(subtype != null) {
			val targetSubtypes = runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
	
	private fun matchesAndDistinct(element: ParadoxScriptProperty, keyword: String, subtype: String?, names: MutableSet<String>): Boolean {
		val stub = element.stub
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetName = runCatching { stub?.name }.getOrNull() ?: definitionInfo?.name ?: return false
		if(!names.add(targetName)) return false
		if(!targetName.matchesKeyword(keyword)) return false
		if(subtype != null) {
			val targetSubtypes = runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
}

