package icu.windea.pls.lang.index

import icu.windea.pls.model.constraints.ParadoxLocalisationIndexConstraint

/**
 * 本地化声明的名字的受约束的索引。
 *
 * 用于优化和调整符合特定约束的本地化声明的索引逻辑。
 *
 * @see ParadoxLocalisationNameIndex
 * @see ParadoxLocalisationIndexConstraint
 */
abstract class ParadoxLocalisationNameConstrainedIndex: ParadoxLocalisationNameIndex() {
    override fun getVersion() = ChronicleIndexVersions.LocalisationStub

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     *
     * @see ParadoxLocalisationIndexConstraint.Modifier
     */
    class ModifierIndex : ParadoxLocalisationNameConstrainedIndex() {
        override fun getKey() = ChronicleIndexKeys.LocalisationNameForModifier
    }

    /**
     * 用于快速索引与事件相关的本地化。
     *
     * @see ParadoxLocalisationIndexConstraint.Event
     */
    class EventIndex : ParadoxLocalisationNameConstrainedIndex() {
        override fun getKey() = ChronicleIndexKeys.LocalisationNameForEvent
    }
}
