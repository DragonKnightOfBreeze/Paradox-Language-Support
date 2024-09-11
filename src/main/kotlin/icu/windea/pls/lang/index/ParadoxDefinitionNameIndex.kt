package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于名字索引定义声明。
 */
class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findStubIndex<ParadoxDefinitionNameIndex>() }
        val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index")
        
        private const val VERSION = 54 //1.3.21
        private const val CACHE_SIZE = 20 * 1024 //38000+ in stellaris@3.6
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
}
