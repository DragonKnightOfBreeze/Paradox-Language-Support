package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.selector.*

object ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptScriptedVariable>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")
	private const val version = 10 //0.7.4
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun findOne(name: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean, selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>): ParadoxScriptScriptedVariable? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		return if(preferFirst) {
			findFirstElement(name, project, scope) { selector.select(it) }
		} else {
			findLastElement(name, project, scope) { selector.select(it) }
		} ?: selector.defaultValue
	}
	
	fun findAll(name: String, project: Project, scope: GlobalSearchScope, selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>): Set<ParadoxScriptScriptedVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		processAllElements(name, project, scope) {
			if(selector.selectAll(it)) result.add(it)
			true
		}
		return result
	}
	
	fun findAll(project: Project, scope: GlobalSearchScope, distinct: Boolean, selector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>): Set<ParadoxScriptScriptedVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		processAllElementsByKeys(project, scope, keyPredicate = { key -> keysToDistinct?.contains(key) != true }) { key, it ->
			if(selector.selectAll(it)) {
				result.add(it)
				keysToDistinct?.add(key)
			}
			true
		}
		return result
	}
}

