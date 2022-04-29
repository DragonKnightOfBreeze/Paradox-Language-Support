package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxDefinitionProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxDefinitionProperty>("paradox.definition.type.index")
	private const val cacheSize = 4 * 1024
	
	override fun getKey() = key
	
	override fun getCacheSize() = cacheSize
	
	fun exists(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return expression.any { type, subtype ->
			existsElement(type, project, scope) { element -> matches(element, name, subtype) }
		}
	}
	
	fun findOne(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxDefinitionProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return expression.select { type, subtype ->
			if(preferFirst) {
				findFirstElement(type, project, scope) { element -> matches(element, name, subtype) }
			} else {
				findLastElement(type, project, scope) { element -> matches(element, name, subtype) }
			}
		}
	}
	
	fun findAll(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope): List<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return expression.collect { type, subtype ->
			findAllElements(type, project, scope) { element -> matches(element, name, subtype) }
		}
	}
	
	fun findAll(typeExpression: String, project: Project, scope: GlobalSearchScope): List<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return expression.collect { type, subtype ->
			findAllElements(type, project, scope) { element -> matches(element, subtype) }
		}
	}
	
	fun findAllByKeyword(keyword: String, typeExpression: String, project: Project, scope: GlobalSearchScope, maxSize: Int): List<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		//需要保证返回结果的名字的唯一性
		val names = mutableSetOf<String>()
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return expression.collect { type, subtype ->
			findAllElements(type, project, scope, maxSize = maxSize) { element -> matchesAndDistinct(element, keyword, subtype, names) }
		}
	}
	
	private fun matches(element: ParadoxDefinitionProperty, name: String, subtype: String?): Boolean {
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
	
	private fun matches(element: ParadoxDefinitionProperty, subtype: String?): Boolean {
		val stub = element.stub
		val definitionInfo = if(stub == null) element.definitionInfo else null
		if(subtype != null) {
			val targetSubtypes = runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
	
	private fun matchesAndDistinct(element: ParadoxDefinitionProperty, keyword: String, subtype: String?, names: MutableSet<String>): Boolean {
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

