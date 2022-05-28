package icu.windea.pls.localisation.psi

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

//注意这里不能直接访问element.localisationInfo，需要优先通过element.stub获取本地化信息

object ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")
	private const val version = 1
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun exists(name: String, localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		if(localeConfig == null) {
			return existsElement(name, project, scope)
		} else {
			return existsElement(name, project, scope) { element -> localeConfig == element.localeConfig }
		}
	}
	
	fun getOne(name: String, localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean, preferFirst: Boolean): ParadoxLocalisationProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		if(localeConfig == null) {
			if(preferFirst) {
				return findFirstElement(name, project, scope)
			} else {
				return findLastElement(name, project, scope)
			}
		} else {
			if(preferFirst) {
				return findFirstElement(name, project, scope, hasDefault) { element -> localeConfig == element.localeConfig }
			} else {
				return findLastElement(name, project, scope, hasDefault) { element -> localeConfig == element.localeConfig }
			}
		}
	}
	
	fun findAll(name: String, localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val inferParadoxLocale = if(localeConfig == null) inferParadoxLocale() else null
		var index = 0
		return processAllElements(name, project, scope) { element, result ->
			val elementLocale = element.localeConfig
			if(localeConfig == null) {
				//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
				if(inferParadoxLocale == elementLocale) {
					result.add(index++, element)
				} else {
					result.add(element)
				}
			} else {
				if(localeConfig == elementLocale || hasDefault) {
					result.add(element)
				}
			}
			true
		}
	}
	
	fun findAll(localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean, distinct: Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = ParadoxLocalisationNameIndex.getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		
		val inferParadoxLocale = if(localeConfig == null) inferParadoxLocale() else null
		val result: MutableList<ParadoxLocalisationProperty> = SmartList()
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		var index = 0
		StubIndex.getInstance().processAllKeys(this.key, project) { key ->
			if(keysToDistinct != null && !keysToDistinct.add(key)) return@processAllKeys true
			ProgressManager.checkCanceled()
			var nextIndex = index
			StubIndex.getInstance().processElements(this.key, key, project, scope, ParadoxLocalisationProperty::class.java) { element ->
				ProgressManager.checkCanceled()
				val elementLocale = element.localeConfig
				if(localeConfig == null) {
					//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
					if(elementLocale == inferParadoxLocale) {
						result.add(index++, element)
						nextIndex++
					} else {
						result.add(element)
						nextIndex++
					}
				} else if(localeConfig == elementLocale || hasDefault) {
					result.add(element)
					nextIndex++
				}
				true
			}
			index = nextIndex
			true
		}
		return result
	}
	
	fun findAllByKeyword(keyword: String, project: Project, scope: GlobalSearchScope, maxSize: Int): Set<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		//需要保证返回结果的名字的唯一性
		val result = CollectionFactory.createSmallMemoryFootprintLinkedSet<ParadoxLocalisationProperty>() //优化性能
		val inferredParadoxLocale = inferParadoxLocale()
		if(keyword.isEmpty()) {
			findFirstElementByKeys(result, project, scope, maxSize = maxSize, hasDefault = true) { element ->
				element.localeConfig == inferredParadoxLocale
			}
		} else {
			findFirstElementByKeys(result, project, scope, maxSize = maxSize, hasDefault = true,
				keyPredicate = { key -> key.matchesKeyword(keyword) }) { element ->
				element.localeConfig == inferredParadoxLocale
			}
		}
		return result
	}
}