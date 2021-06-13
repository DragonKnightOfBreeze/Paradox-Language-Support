package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.model.*

object ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 2 * 1024 
	
	fun getOne(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean, preferFirst: Boolean): ParadoxLocalisationProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxLocalisationProperty::class.java)
		return if(preferFirst) {
			elements.firstOrNull { element -> locale == null || locale == element.paradoxLocale } ?: if(hasDefault) elements.firstOrNull() else null
		} else {
			elements.lastOrNull { element -> locale == null || locale == element.paradoxLocale } ?: if(hasDefault) elements.lastOrNull() else null
		}
	}
	
	fun getAll(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		val elements = StubIndex.getElements(getKey(), name, project, scope, ParadoxLocalisationProperty::class.java)
		for(element in elements) {
			val elementLocale = element.paradoxLocale
			if(locale == null) {
				//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
				if(elementLocale == inferParadoxLocale()) {
					result.add(index++, element)
				} else {
					result.add(element)
				}
			} else {
				if(locale == elementLocale || hasDefault) {
					result.add(element)
				}
			}
		}
		return result
	}
	
	fun getAll(locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		for(key in keys) {
			val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxLocalisationProperty::class.java)
			var nextIndex = index
			for(element in elements) {
				val elementLocale = element.paradoxLocale
				if(locale == null) {
					//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
					if(elementLocale == inferParadoxLocale()) {
						result.add(index++, element)
						nextIndex++
					} else {
						result.add(element)
						nextIndex++
					}
				} else if(locale == elementLocale || hasDefault) {
					result.add(element)
					nextIndex++
				}
			}
			index = nextIndex
		}
		return result
	}
	
	inline fun filter(locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean, predicate: (String) -> Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		for(key in keys) {
			if(predicate(key)) {
				val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxLocalisationProperty::class.java)
				var nextIndex = index
				for(element in elements) {
					val elementLocale = element.paradoxLocale
					if(locale == null) {
						//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
						if(elementLocale == inferParadoxLocale()) {
							result.add(index++, element)
							nextIndex++
						} else {
							result.add(element)
							nextIndex++
						}
					} else if(locale == elementLocale || hasDefault) {
						result.add(element)
						nextIndex++
					}
				}
				index = nextIndex
			}
		}
		return result
	}
	
	fun findByKeyword(keyword: String, project: Project, scope: GlobalSearchScope): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		val matchedKeys = keys.filter { it.contains(keyword, ignoreCase = true) }
		val result = mutableListOf<ParadoxLocalisationProperty>()
		for(key in matchedKeys) {
			val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxLocalisationProperty::class.java)
			val firstElement = elements.find { it.paradoxLocale == inferParadoxLocale() } ?: elements.firstOrNull()
			if(firstElement != null) result.add(firstElement)
		}
		return result
	}
}