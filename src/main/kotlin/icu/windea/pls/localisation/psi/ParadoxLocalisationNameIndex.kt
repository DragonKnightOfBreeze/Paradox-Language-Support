package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.model.*

object ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 100 * 1024 //50000+
	
	fun getOne(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope,hasDefault:Boolean,preferFirst:Boolean): ParadoxLocalisationProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxLocalisationProperty::class.java)
		return if(preferFirst){
			elements.firstOrNull { element->locale == null || locale == element.paradoxLocale } ?: if(hasDefault) elements.firstOrNull() else null 
		} else{
			elements.lastOrNull { element -> locale == null || locale == element.paradoxLocale }?: if(hasDefault) elements.lastOrNull() else null
		}
	}
	
	fun getAll(name: String, locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope,hasDefault:Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		val elements = StubIndex.getElements(this.key, name, project, scope, ParadoxLocalisationProperty::class.java)
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
	
	fun getAll(names:Iterable<String>,locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope,hasDefault:Boolean,keepOrder:Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		val keys = getAllKeys(project)
		for(key in keys) {
			if(key in names) {
				val group = get(key, project, scope)
				var nextIndex = index
				for(element in group) {
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
		if(keepOrder) result.sortBy { names.indexOf(it.name) } 
		return result
	}
	
	fun getAll(locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope,hasDefault:Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		val keys = getAllKeys(project)
		for(key in keys) {
			val group = get(key, project, scope)
			var nextIndex = index
			for(element in group) {
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
	
	inline fun filter(locale: ParadoxLocale?, project: Project, scope: GlobalSearchScope, hasDefault: Boolean,predicate: (String) -> Boolean): List<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val result = mutableListOf<ParadoxLocalisationProperty>()
		var index = 0
		val keys = getAllKeys(project)
		for(key in keys) {
			if(predicate(key)) {
				val group = get(key, project, scope)
				var nextIndex = index
				for(element in group) {
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
}
