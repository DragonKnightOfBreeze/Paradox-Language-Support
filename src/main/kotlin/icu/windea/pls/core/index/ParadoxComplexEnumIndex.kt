package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

object ParadoxComplexEnumIndex: StringStubIndexExtension<ParadoxScriptExpressionElement>(){
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptExpressionElement>("paradox.complexEnum.index")
	private const val version = 12 //0.7.6
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
}