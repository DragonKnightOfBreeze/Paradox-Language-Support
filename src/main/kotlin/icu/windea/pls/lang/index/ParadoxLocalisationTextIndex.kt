package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 用于基于本地化文本（移除了大部分特殊格式）索引本地化声明。
 */
class ParadoxLocalisationTextIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        private const val VERSION = 66 //1.4.2
        private const val CACHE_SIZE = 100 * 1024 //98000+ in stellaris@3.6
    }

    override fun getKey() = ParadoxIndexManager.LocalisationTextKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
