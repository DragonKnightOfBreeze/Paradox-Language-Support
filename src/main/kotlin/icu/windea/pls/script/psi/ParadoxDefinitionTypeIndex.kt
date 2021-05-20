package icu.windea.pls.script.psi

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptProperty>("paradox.definition.type.index")
	
	override fun getKey() = key
	
	override fun getCacheSize() = 4 * 1024
	
	fun getAll(type:String, project: Project, scope: GlobalSearchScope): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val elements = StubIndex.getElements(this.key, type, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			result.add(element)
		}
		return result
	}
	
	inline fun filter(type:String,project: Project, scope: GlobalSearchScope, predicate:(String)->Boolean): List<ParadoxScriptProperty> {
		val result = mutableListOf<ParadoxScriptProperty>()
		val elements = StubIndex.getElements(this.getKey(), type, project, scope, ParadoxScriptProperty::class.java)
		for(element in elements) {
			val name = element.paradoxDefinitionInfo?.name
			if(name != null && predicate(name)) result.add(element)
		}
		return result
	}
}