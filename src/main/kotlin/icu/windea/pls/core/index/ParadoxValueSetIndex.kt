package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

object ParadoxValueSetIndex : StringStubIndexExtension<ParadoxScriptString>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptString>("paradox.valueSet.index")
	private const val version = 13 //0.7.11
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
}
