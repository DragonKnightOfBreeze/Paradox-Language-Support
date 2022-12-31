package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

//注意这里不能直接访问element.localisationInfo，需要优先通过element.stub获取本地化信息

sealed class AbstractParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
	fun findOne(name: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ParadoxLocalisationProperty? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		return if(preferFirst) {
			findFirstElement(name, project, scope) { selector.select(it) }
		} else {
			findLastElement(name, project, scope) { selector.select(it) }
		} ?: selector.defaultValue
	}
	
	fun findAll(name: String, project: Project, scope: GlobalSearchScope, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): Set<ParadoxLocalisationProperty> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		processAllElements(name, project, scope) {
			if(selector.selectAll(it)) result.add(it)
			true
		}
		return result
	}
	
	//因为localisation可能过多（数千个），这里我们使用特殊的内联process方法，且预先仅接收第一个相同key的元素 & 预先过滤不匹配关键字的
	
	inline fun processVariants(
		keyword: String,
		project: Project,
		scope: GlobalSearchScope,
		maxSize: Int,
		selector: ChainedParadoxSelector<ParadoxLocalisationProperty>,
		crossinline processor: ProcessEntry.(element: ParadoxLocalisationProperty) -> Boolean
	): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return true
		
		//注意：如果不预先过滤，结果可能过多（10w+）
		//需要保证返回结果的名字的唯一性
		val noKeyword = keyword.isEmpty()
		return processFirstElementByKeys(project, scope, maxSize = maxSize,
			keyPredicate = { key -> noKeyword || key.matchesKeyword(keyword) },
			predicate = { element -> selector.select(element) },
			getDefaultValue = { selector.defaultValue },
			resetDefaultValue = { selector.defaultValue = null },
			processor = processor
		)
	}
}