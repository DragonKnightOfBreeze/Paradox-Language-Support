package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*

//注意这里不能直接访问element.localisationInfo，需要优先通过element.stub获取本地化信息

sealed class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	object Localisation : ParadoxLocalisationNameIndex() {
		private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
		private const val version = indexVersion
		private const val cacheSize = 200 * 1024
		
		override fun getKey() = key
		override fun getVersion() = version
		override fun getCacheSize() = cacheSize
	}
	
	object SyncedLocalisation : ParadoxLocalisationNameIndex() {
		private val key = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")
		private const val version = indexVersion
		private const val cacheSize = 2 * 1024
		
		override fun getKey() = key
		override fun getVersion() = version
		override fun getCacheSize() = cacheSize
	}
	
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
	
	inline fun processVariants(keyword: String, project: Project, scope: GlobalSearchScope, maxSize: Int, crossinline processor: ProcessEntry.(element: ParadoxLocalisationProperty) -> Boolean): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return true
		
		//注意：如果不预先过滤，结果可能过多（10w+）
		//需要保证返回结果的名字的唯一性
		val noKeyword = keyword.isEmpty()
		val inferredParadoxLocale = inferParadoxLocale()
		return processFirstElementByKeys(project,scope, hasDefault = true, maxSize = maxSize, 
			keyPredicate = {key -> noKeyword || key.matchesKeyword(keyword)},
			predicate = {element -> element.localeConfig == inferredParadoxLocale},
			processor = processor
		)
	}
}