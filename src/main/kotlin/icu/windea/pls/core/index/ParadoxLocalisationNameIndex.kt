package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
        private const val VERSION = 15 //0.8.3
        private const val CACHE_SIZE = 100 * 1024
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
    
}