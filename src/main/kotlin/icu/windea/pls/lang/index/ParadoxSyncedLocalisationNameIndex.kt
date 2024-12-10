package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引同步本地化声明。
 */
class ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findStubIndex<ParadoxSyncedLocalisationNameIndex>() }
        val KEY = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")

        private const val VERSION = 58 //1.3.27
        private const val CACHE_SIZE = 2 * 1024
    }

    override fun getKey() = KEY

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
