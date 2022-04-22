package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

object ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("localisation.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 100 * 1024 //50000+
	
	fun exists(name: String, localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		if(localeConfig == null) {
			return existsElement(name, project, scope)
		} else {
			return existsElement(name, project, scope) { element -> localeConfig == element.localeConfig }
		}
	}
	
	fun findOne(name: String, localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean, preferFirst: Boolean): ParadoxLocalisationProperty? {
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
		
		var index = 0
		return processAllElements(name, project, scope) { element, result ->
			val elementLocale = element.localeConfig
			if(localeConfig == null) {
				//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
				if(inferParadoxLocale() == elementLocale) {
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
	
	//fun findAll(locale: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean): List<ParadoxLocalisationProperty> {
	//	//如果索引未完成
	//	if(DumbService.isDumb(project)) return emptyList()
	//	
	//	val keys = getAllKeys(project)
	//	if(keys.isEmpty()) return emptyList()
	//	val result = mutableListOf<ParadoxLocalisationProperty>()
	//	var index = 0
	//	for(key in keys) {
	//		val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxLocalisationProperty::class.java)
	//		var nextIndex = index
	//		for(element in elements) {
	//			val elementLocale = element.localeConfig
	//			if(locale == null) {
	//				//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
	//				if(elementLocale == inferParadoxLocale()) {
	//					result.add(index++, element)
	//					nextIndex++
	//				} else {
	//					result.add(element)
	//					nextIndex++
	//				}
	//			} else if(locale == elementLocale || hasDefault) {
	//				result.add(element)
	//				nextIndex++
	//			}
	//		}
	//		index = nextIndex
	//	}
	//	return result
	//}
	
	fun findAllByKeyword(keyword: String, project: Project, scope: GlobalSearchScope, maxSize: Int): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		//需要保证返回结果的名字的唯一性
		if(keyword.isEmpty()) {
			return findFirstElementByKeys(project, scope, maxSize = maxSize, hasDefault = true) { element ->
				element.localeConfig == inferParadoxLocale()
			}
		} else {
			return findFirstElementByKeys(project, scope, maxSize = maxSize, hasDefault = true,
				keyPredicate = { key -> matches(key, keyword) }) { element ->
				element.localeConfig == inferParadoxLocale()
			}
		}
	}
	
	fun findAllByNames(names: Iterable<String>, localeConfig: ParadoxLocaleConfig?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean, keepOrder: Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keys = getAllKeys(project)
		if(keys.isEmpty()) return emptyList()
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		for(key in keys) {
			if(key in names) {
				val elements = StubIndex.getElements(getKey(), key, project, scope, ParadoxLocalisationProperty::class.java)
				if(elements.isEmpty()) continue
				var nextIndex = index
				for(element in elements) {
					val elementLocale = element.localeConfig
					if(localeConfig == null) {
						//需要将用户的语言区域对应的本地化属性放到该组本地化属性的最前面
						if(elementLocale == inferParadoxLocale()) {
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
				}
				index = nextIndex
			}
		}
		if(keepOrder) result.sortBy { names.indexOf(it.name) }
		return result
	}
	
	private fun matches(key: String, keyword: String): Boolean {
		return key.matchesKeyword(keyword)
	}
}



