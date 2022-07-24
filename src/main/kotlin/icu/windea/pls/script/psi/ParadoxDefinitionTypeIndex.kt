package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.util.selector.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxDefinitionProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxDefinitionProperty>("paradox.definition.type.index")
	private const val version = indexVersion
	private const val cacheSize = 4 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun findOne(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean, selector: ChainedParadoxSelector<ParadoxDefinitionProperty>): ParadoxDefinitionProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		return expression.select { type, subtype ->
			if(preferFirst) {
				findFirstElement(type, project, scope) { matches(it, name, subtype) && selector.select(it) }
			} else {
				findLastElement(type, project, scope) { matches(it, name, subtype) && selector.select(it) }
			}
		} ?: selector.defaultValue
	}
	
	fun findAll(name: String, typeExpression: String, project: Project, scope: GlobalSearchScope, selector: ChainedParadoxSelector<ParadoxDefinitionProperty>): Set<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		expression.selectAll { type, subtype ->
			processAllElements(type, project, scope) {
				if(matches(it, name, subtype) && selector.selectAll(it)) result.add(it)
				true
			}
		}
		return result
	}
	
	fun findAll(typeExpression: String, project: Project, scope: GlobalSearchScope, distinct: Boolean, selector: ChainedParadoxSelector<ParadoxDefinitionProperty>): Set<ParadoxDefinitionProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
		expression.selectAll { type, subtype ->
			val namesToDistinct = if(distinct) mutableSetOf<String>() else null
			processAllElements(type, project, scope) {
				if(matches(it, subtype, namesToDistinct) && selector.selectAll(it)) {
					result.add(it)
					namesToDistinct?.add(getName(it).orEmpty())
				}
				true
			}
		}
		return result
	}
	
	//fun findAllByKeyword(keyword: String, typeExpression: String, project: Project, scope: GlobalSearchScope, maxSize: Int): List<ParadoxDefinitionProperty> {
	//	//如果索引未完成
	//	if(DumbService.isDumb(project)) return emptyList()
	//	
	//	//需要保证返回结果的名字的唯一性
	//	val names = mutableSetOf<String>()
	//	val expression = ParadoxDefinitionTypeExpression.resolve(typeExpression)
	//	return expression.collect { type, subtype ->
	//		findAllElements(type, project, scope, maxSize = maxSize) { element -> matchesAndDistinct(element, keyword, subtype, names) }
	//	}
	//}
	
	//以下匹配方法只能定义在ParadoxDefinitionTypeExpression之外
	
	private fun matches(element: ParadoxDefinitionProperty, name: String, subtype: String?): Boolean {
		val stub = element.getStub()
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetName = getName(stub, definitionInfo) ?: return false
		if(targetName != name) return false
		if(subtype != null) {
			val targetSubtypes = getSubtypes(stub, definitionInfo) ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
	
	private fun matches(element: ParadoxDefinitionProperty, subtype: String?, namesToDistinct: MutableSet<String>? = null): Boolean {
		val stub = element.getStub()
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetName = getName(stub, definitionInfo) ?: return false
		if(namesToDistinct?.contains(targetName) == true) return false
		if(subtype != null) {
			val targetSubtypes = getSubtypes(stub, definitionInfo) ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
	
	private fun getName(element: ParadoxDefinitionProperty): String? {
		return runCatching { element.getStub()?.name }.getOrNull() ?: element.definitionInfo?.name
	}
	
	private fun getName(stub: ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>?, definitionInfo: ParadoxDefinitionInfo?): String? {
		return runCatching { stub?.name }.getOrNull() ?: definitionInfo?.name
	}
	
	private fun getSubtypes(stub: ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>?, definitionInfo: ParadoxDefinitionInfo?): List<String>? {
		return runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes
	}
	
}

