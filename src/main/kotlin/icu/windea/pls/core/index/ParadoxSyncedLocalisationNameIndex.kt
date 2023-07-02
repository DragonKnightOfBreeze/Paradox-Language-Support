package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引同步本地化声明。
 */
class ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")
        private const val VERSION = 31 //1.1.1
        private const val CACHE_SIZE = 2 * 1024
    }
    
    override fun getKey() = KEY
    override fun getVersion() = VERSION
    override fun getCacheSize() = CACHE_SIZE
}
