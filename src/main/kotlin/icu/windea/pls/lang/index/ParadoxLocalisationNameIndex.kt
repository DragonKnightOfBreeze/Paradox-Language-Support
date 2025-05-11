package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于基于名字索引本地化声明。
 */
class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        private const val VERSION = 60 //1.4.0
        private const val CACHE_SIZE = 100 * 1024 //98000+ in stellaris@3.6
    }

    override fun getKey() = ParadoxIndexManager.LocalisationNameKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE

    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     */
    class ModifierIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        companion object {
            private const val VERSION = 60 //1.4.0
            private const val CACHE_SIZE = 2 * 1024
        }

        override fun getKey() = ParadoxIndexManager.LocalisationNameForModifierKey

        override fun getVersion() = VERSION

        override fun getCacheSize() = CACHE_SIZE
    }
}
