package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引本地化声明。
 */
class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        private const val VERSION = 72 //2.0.2
        private const val CACHE_SIZE = 100 * 1024 //98000+ in stellaris@3.6
    }

    override fun getKey() = ParadoxIndexManager.LocalisationNameKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     */
    class ModifierIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexManager.LocalisationNameForModifierKey
        override fun getVersion() = VERSION
    }

    /**
     * 用于快速索引与事件相关的本地化。
     */
    class EventIndex: StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexManager.LocalisationNameForEventKey
        override fun getVersion() = VERSION
    }

    /**
     * 用于快速索引与科技相关的本地化。
     */
    class TechIndex: StringStubIndexExtension<ParadoxLocalisationProperty>() {
        override fun getKey() = ParadoxIndexManager.LocalisationNameForTechKey
        override fun getVersion() = VERSION
    }
}
