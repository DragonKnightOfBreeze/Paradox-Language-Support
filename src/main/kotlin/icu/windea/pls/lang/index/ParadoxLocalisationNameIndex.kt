package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import  icu.windea.pls.model.constraints.ParadoxIndexConstraint

/**
 * 基于名字索引本地化声明。
 */
class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = ParadoxIndexKeys.LocalisationName
    override fun getVersion() = 76 // VERSION for 2.0.6
    override fun getCacheSize() = 32 * 1024 // CACHE SIZE - 98000+ in stellaris@3.6

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     *
     * @see ParadoxIndexConstraint.Localisation.Modifier
     */
    class ModifierIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexKeys.LocalisationNameForModifier
        override fun getVersion() = 76 // VERSION for 2.0.6
    }

    /**
     * 用于快速索引与事件相关的本地化。
     *
     * @see ParadoxIndexConstraint.Localisation.Event
     */
    class EventIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexKeys.LocalisationNameForEvent
        override fun getVersion() = 76 // VERSION for 2.0.6
    }

    /**
     * 用于快速索引与科技相关的本地化。
     *
     * @see ParadoxIndexConstraint.Localisation.Tech
     */
    class TechIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexKeys.LocalisationNameForTech
        override fun getVersion() = 76 // VERSION for 2.0.6
    }
}
