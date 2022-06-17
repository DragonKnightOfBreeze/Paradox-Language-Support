package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.model.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxDefinitionProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxDefinitionProperty>("paradox.definition.name.index")
	private const val version = 4
	private const val cacheSize = 4 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun exists(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		if(typeExpression == null) return existsElement(name, project, scope)
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return existsElement(name, project, scope) { element -> expression.matches(element) }
	}
	
	fun findOne(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxDefinitionProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		if(preferFirst) {
			if(typeExpression == null) return findFirstElement(name, project, scope)
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			return findFirstElement(name, project, scope) { element -> expression.matches(element) }
		} else {
			if(typeExpression == null) return findLastElement(name, project, scope)
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			return findLastElement(name, project, scope) { element -> expression.matches(element) }
		}
	}
	
	fun findAll(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope): List<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		if(typeExpression == null) return findAllElements(name, project, scope)
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return findAllElements(name, project, scope) { element -> expression.matches(element) }
	}
	
	fun findAll(typeExpression: String?, project: Project, scope: GlobalSearchScope, distinct: Boolean): Set<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = mutableSetOf<ParadoxDefinitionProperty>()
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		if(typeExpression == null) {
			findAllElementsByKeys(result, project, scope, keyPredicate = { key -> keysToDistinct?.add(key) ?: true })
		} else {
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			findAllElementsByKeys(result, project, scope, keyPredicate = { key -> keysToDistinct?.add(key) ?: true }) { element ->
				expression.matches(element)
			}
		}
		return result
	}
}
