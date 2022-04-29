package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

//注意这里不能直接访问element.localisationInfo，需要优先通过element.stub获取本地化信息

object ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
	private const val cacheSize = 8 * 1024
	
	override fun getKey() = key
	
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
	
	private fun matches(key: String, keyword: String): Boolean {
		return key.matchesKeyword(keyword)
	}
}



