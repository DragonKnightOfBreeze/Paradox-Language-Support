package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

object ParadoxComplexEnumIndex: StringStubIndexExtension<ParadoxScriptStringExpressionElement>(){
	private val key = StubIndexKey.createIndexKey<String, ParadoxScriptStringExpressionElement>("paradox.complexEnum.index")
	private const val version = 14 //0.7.13
	private const val cacheSize = 256
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
}