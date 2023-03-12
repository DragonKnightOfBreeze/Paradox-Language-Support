package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxComplexEnumValueIndex : StringStubIndexExtension<ParadoxScriptStringExpressionElement>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptStringExpressionElement>("paradox.complexEnumValue.index")
        private const val VERSION = 16 //0.9.0
        private const val CACHE_SIZE = 2 * 1024 //1300+ in stellaris@3.6
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
}

