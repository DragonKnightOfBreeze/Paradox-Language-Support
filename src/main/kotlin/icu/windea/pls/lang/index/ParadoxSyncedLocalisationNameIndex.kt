package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引同步本地化声明。
 */
class ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        private const val VERSION = 60 //1.4.0
        private const val CACHE_SIZE = 2 * 1024
    }

    override fun getKey() = ParadoxIndexManager.SyncedLocalisationNameKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
