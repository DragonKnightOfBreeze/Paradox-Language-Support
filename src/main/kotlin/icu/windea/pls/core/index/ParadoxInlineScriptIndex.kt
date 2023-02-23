package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxInlineScriptIndex: StringStubIndexExtension<ParadoxScriptPropertyKey>(){
	companion object {
		@JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptPropertyKey>("paradox.inlineScript.index")
		private const val VERSION = 14 //0.7.13
		private const val CACHE_SIZE = 2 * 1024
	}
	
	override fun getKey() = KEY
	
	override fun getVersion() = VERSION
	
	override fun getCacheSize() = CACHE_SIZE
}