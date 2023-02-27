package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
	companion object {
		@JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.type.index")
		private const val VERSION = 15 //0.8.3
		private const val CACHE_SIZE = 2 * 1024
	}
	
	override fun getKey() = KEY
	
	override fun getVersion() = VERSION
	
	override fun getCacheSize() = CACHE_SIZE
}

