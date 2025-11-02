package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 同步本地化声明的名字的索引。
 */
class ParadoxSyncedLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = PlsIndexKeys.SyncedLocalisationName

    override fun getVersion() = PlsIndexVersions.LocalisationStub
}
