package icu.windea.pls.lang.index

import icu.windea.pls.lang.index.constraints.ParadoxLocalisationIndexConstraint

/**
 * 本地化声明的名字的受约束索引。
 *
 * 用于优化和调整符合特定约束的本地化声明的索引逻辑。
 *
 * @see ParadoxLocalisationNameIndex
 * @see ParadoxLocalisationIndexConstraint
 */
abstract class ParadoxLocalisationNameConstrainedIndex : ParadoxLocalisationNameIndex() {
    abstract val constraint: ParadoxLocalisationIndexConstraint

    override fun getKey() = constraint.indexKey

    override fun getVersion() = ChronicleIndexVersions.LocalisationStub

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     *
     * @see ParadoxLocalisationIndexConstraint.Modifier
     */
    class ModifierIndex : ParadoxLocalisationNameConstrainedIndex() {
        override val constraint get() = ParadoxLocalisationIndexConstraint.Modifier
    }

    /**
     * 用于快速索引与事件相关的本地化。
     *
     * @see ParadoxLocalisationIndexConstraint.Event
     */
    class EventIndex : ParadoxLocalisationNameConstrainedIndex() {
        override val constraint get() = ParadoxLocalisationIndexConstraint.Event
    }
}
