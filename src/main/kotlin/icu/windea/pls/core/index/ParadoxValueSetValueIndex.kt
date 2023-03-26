package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxValueSetValueIndex : StringStubIndexExtension<ParadoxScriptString>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptString>("paradox.valueSetValue.index")
        private const val VERSION = 17 //0.9.2
        private const val CACHE_SIZE = 2048
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
}