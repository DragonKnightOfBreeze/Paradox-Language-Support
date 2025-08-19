package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引同步本地化声明。
 */
class ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = ParadoxIndexKeys.SyncedLocalisationName

    override fun getVersion() = 72 // VERSION for 2.0.2
}
