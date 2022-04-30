package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxScriptVariableNameIndex : StringStubIndexExtension<ParadoxScriptVariable>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptVariable>("paradox.scriptVariable.name.index")
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getCacheSize() = cacheSize
	
	fun exists(name: String, project: Project, scope: GlobalSearchScope): Boolean {
		//如果索引未完成
		if(DumbService.isDumb(project)) return false
		
		return existsElement(name, project, scope)
	}
	
	fun findOne(name: String, project: Project, scope: GlobalSearchScope, preferFirst: Boolean): ParadoxScriptVariable? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		return if(preferFirst) findFirstElement(name, project, scope) else findLastElement(name, project, scope)
	}
	
	fun findAll(name: String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		return findAllElements(name, project, scope)
	}
	
	fun findAll(project: Project, scope: GlobalSearchScope, distinct: Boolean): List<ParadoxScriptVariable> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptyList()
		
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		return findAllElementsByKeys(project, scope, keyPredicate = { key -> keysToDistinct?.add(key) ?: true })
	}
}

