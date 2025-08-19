package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引本地化声明。
 */
class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    override fun getKey() = ParadoxIndexKeys.LocalisationName

    override fun getVersion() = 72 // VERSION for 2.0.2

    override fun getCacheSize() = 32 * 1024 // CACHE SIZE - 98000+ in stellaris@3.6

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     */
    class ModifierIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexKeys.LocalisationNameForModifier
        override fun getVersion() = 72 // VERSION for 2.0.2
    }

    /**
     * 用于快速索引与事件相关的本地化。
     */
    class EventIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexKeys.LocalisationNameForEvent
        override fun getVersion() = 72 // VERSION for 2.0.2
    }

    /**
     * 用于快速索引与科技相关的本地化。
     */
    class TechIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexKeys.LocalisationNameForTech
        override fun getVersion() = 72 // VERSION for 2.0.2
    }
}
