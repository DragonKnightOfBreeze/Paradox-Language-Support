package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.selector.*

object ParadoxValueSetValueIndex : StringStubIndexExtension<ParadoxScriptString>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptString>("paradox.valueSetValue.index")
	private const val version = 9 //0.7.3
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
	
	fun findOne(name: String, valueSetName: String, project: Project, scope: GlobalSearchScope, selector: ChainedParadoxSelector<ParadoxScriptString>): ParadoxScriptString? {
		//如果索引未完成
		if(DumbService.isDumb(project)) return null
		
		return findFirstElement(valueSetName, project, scope) { matches(it, name) && selector.select(it) } ?: selector.defaultValue
	}
	
	fun findAll(name: String, valueSetName: String, project: Project, scope: GlobalSearchScope, selector: ChainedParadoxSelector<ParadoxScriptString>): Set<ParadoxScriptString> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		processAllElements(valueSetName, project, scope) {
			if(matches(it, name) && selector.selectAll(it)) result.add(it)
			true
		}
		return result
	}
	
	fun findAll(valueSetName: String, project: Project, scope: GlobalSearchScope, distinct: Boolean, selector: ChainedParadoxSelector<ParadoxScriptString>): Set<ParadoxScriptString> {
		//如果索引未完成
		if(DumbService.isDumb(project)) return emptySet()
		
		val result = MutableSet(selector.comparator())
		val keysToDistinct = if(distinct) mutableSetOf<String>() else null
		processAllElements(valueSetName, project, scope) {
			val valueName = getName(it) ?: return@processAllElements true
			if((keysToDistinct?.contains(valueName) != true) && selector.selectAll(it)) {
				result.add(it)
				keysToDistinct?.add(valueName)
			}
			true
		}
		return result
	}
	
	private fun matches(it: ParadoxScriptString, valueName: String): Boolean {
		return getName(it) == valueName
	}
	
	private fun getName(it: ParadoxScriptString): String? {
		return it.stub?.valueSetValueInfo?.name?.takeIfNotEmpty()
	}
}