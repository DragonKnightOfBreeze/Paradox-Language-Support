package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.psi.*

object ParadoxComplexEnumIndex: StringStubIndexExtension<ParadoxExpressionAwareElement>(){
	private val key = StubIndexKey.createIndexKey<String, ParadoxExpressionAwareElement>("paradox.complexEnum.index")
	private const val version = 10 //0.7.4
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
}