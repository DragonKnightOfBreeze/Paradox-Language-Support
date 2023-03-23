package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxValueSetIndex : StringStubIndexExtension<ParadoxScriptString>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptString>("paradox.valueSet.index")
        private const val VERSION = 17 //0.9.2
        private const val CACHE_SIZE = 256 //29 in stellaris@3.6
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
}
