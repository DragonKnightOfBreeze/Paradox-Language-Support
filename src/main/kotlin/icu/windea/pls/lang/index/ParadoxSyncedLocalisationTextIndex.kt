package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 用于基于本地化文本（移除了大部分特殊格式）索引同步本地化声明。
 */
class ParadoxSyncedLocalisationTextIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        private const val VERSION = 66 //1.4.2
        private const val CACHE_SIZE = 2 * 1024
    }

    override fun getKey() = ParadoxIndexManager.SyncedLocalisationTextKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
