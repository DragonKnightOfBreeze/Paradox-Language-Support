package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.name.index")
        private const val VERSION = 15 //0.8.3
        private const val CACHE_SIZE = 20 * 1024 //38000+ in stellaris@3.6
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
}
