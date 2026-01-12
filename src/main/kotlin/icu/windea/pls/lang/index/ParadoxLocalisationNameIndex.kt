package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxLocalisationIndexConstraint

/**
 * 本地化声明的名字的索引。
 */
class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = PlsIndexKeys.LocalisationName

    override fun getVersion() = PlsIndexVersions.LocalisationStub

    override fun getCacheSize() = 32 * 1024 // CACHE SIZE - 98000+ in stellaris@3.6

    /**
     * @see ParadoxLocalisationIndexConstraint
     */
    sealed class BaseIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getVersion() = PlsIndexVersions.LocalisationStub
    }

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     *
     * @see ParadoxLocalisationIndexConstraint.Modifier
     */
    class ModifierIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.LocalisationNameForModifier
    }

    /**
     * 用于快速索引与事件相关的本地化。
     *
     * @see ParadoxLocalisationIndexConstraint.Event
     */
    class EventIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.LocalisationNameForEvent
    }

    /**
     * 用于快速索引与科技相关的本地化。
     *
     * @see ParadoxLocalisationIndexConstraint.Tech
     */
    class TechIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.LocalisationNameForTech
    }
}
