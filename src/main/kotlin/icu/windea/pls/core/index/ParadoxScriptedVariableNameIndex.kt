package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptScriptedVariable>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")
        private const val VERSION = 15 //0.8.3
        private const val CACHE_SIZE = 2 * 1024 //700+ in stellaris@3.6
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
}

