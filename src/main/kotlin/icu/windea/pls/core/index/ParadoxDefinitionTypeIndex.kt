package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.core.psi.*

//注意这里不能直接访问element.definitionInfo，需要优先通过element.stub获取定义信息

object ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxDefinitionProperty>() {
	private val key = StubIndexKey.createIndexKey<String, ParadoxDefinitionProperty>("paradox.definition.type.index")
	private const val version = 11 //0.7.4
	private const val cacheSize = 2 * 1024
	
	override fun getKey() = key
	
	override fun getVersion() = version
	
	override fun getCacheSize() = cacheSize
}

