package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.selector.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxDefinitionProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxDefinitionProperty>("paradox.definition.name.index")
	private const val version = 10 //0.7.4
	private const val cacheSize = 4 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun findOne(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope, preferFirst: Boolean, selector: ChainedParadoxSelector<ParadoxDefinitionProperty>): ParadoxDefinitionProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		return if(preferFirst) {
			if(typeExpression == null) return findFirstElement(name, project, scope) { selector.select(it) }
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			findFirstElement(name, project, scope) { expression.matches(it) && selector.select(it) }
		} else {
			if(typeExpression == null) return findLastElement(name, project, scope) { selector.select(it) }
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			findLastElement(name, project, scope) { expression.matches(it) && selector.select(it) }
		} ?: selector.defaultValue
	}
	
	fun findAll(name: String, typeExpression: String?, project: Project, scope: GlobalSearchScope, selector: ChainedParadoxSelector<ParadoxDefinitionProperty>): Set<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		if(typeExpression == null) {
			processAllElements(name, project, scope) {
				if(selector.selectAll(it)) result.add(it)
				true
			}
		} else {
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			processAllElements(name, project, scope) {
				if(expression.matches(it) && selector.selectAll(it)) result.add(it)
				true
			}
		}
		return result
	}
	
	fun findAll(typeExpression: String?, project: Project, scope: GlobalSearchScope, distinct: Boolean, selector: ChainedParadoxSelector<ParadoxDefinitionProperty>): Set<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		if(typeExpression == null) {
			processAllElementsByKeys(project, scope, keyPredicate = { key -> keysToDistinct?.contains(key) != true }) { key, it ->
				if(selector.selectAll(it)) {
					result.add(it)
					keysToDistinct?.add(key)
				}
				true
			}
		} else {
			val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
			processAllElementsByKeys(project, scope, keyPredicate = { key -> keysToDistinct?.contains(key) != true }) { key, it ->
				if(expression.matches(it) && selector.selectAll(it)) {
					result.add(it)
					keysToDistinct?.add(key)
				}
				true
			}
		}
		return result
	}
}
