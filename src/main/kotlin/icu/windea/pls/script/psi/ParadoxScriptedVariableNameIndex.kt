package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.util.selector.*
import java.util.*

object ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptVariable>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptVariable>("paradox.scriptedVariable.name.index")
	private const val version = indexVersion
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun findOne(name: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean, selector: ParadoxSelector<ParadoxScriptVariable>): ParadoxScriptVariable? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		return if(preferFirst) {
			findFirstElement(name, project, scope) { selector.select(it) }
		} else {
			findLastElement(name, project, scope) { selector.select(it) }
		} ?: selector.defaultValue
	}
	
	fun findAll(name: String, project: Project, scope: GlobalSearchScope, selector: ParadoxSelector<ParadoxScriptVariable>): Set<ParadoxScriptVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = TreeSet(selector.comparator())
		processAllElements(name, project, scope) {  
			if(selector.selectAll(it)) result.add(it)
			true
		}
		return result
	}
	
	fun findAll(project: Project, scope: GlobalSearchScope, distinct: Boolean, selector: ParadoxSelector<ParadoxScriptVariable>): Set<ParadoxScriptVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = TreeSet(selector.comparator())
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		processAllElementsByKeys(project, scope, keyPredicate = { key -> keysToDistinct?.add(key) ?: true }) {
			if(selector.selectAll(it)) result.add(it)
			true
		}
		return result
	}
}

