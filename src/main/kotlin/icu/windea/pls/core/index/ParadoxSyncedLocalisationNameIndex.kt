package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

private val KEY = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.syncedLocalisation.name.index")
private const val VERSION = 48 //1.3.1
private const val CACHE_SIZE = 2 * 1024

/**
 * 用于基于名字索引同步本地化声明。
 */
class ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = KEY
    override fun getVersion() = VERSION
    override fun getCacheSize() = CACHE_SIZE
}
