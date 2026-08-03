package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxLocalisationIndexConstraint

/**
 * 本地化声明的名字的索引。
 *
 * @see ParadoxLocalisationNameConstrainedIndex
 * @see ParadoxLocalisationIndexConstraint
 */
open class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = ChronicleIndexKeys.LocalisationName

    override fun getVersion() = ChronicleIndexVersions.LocalisationStub

    override fun getCacheSize() = 32 * 1024 // CACHE SIZE - 98000+ in stellaris@3.6
}
