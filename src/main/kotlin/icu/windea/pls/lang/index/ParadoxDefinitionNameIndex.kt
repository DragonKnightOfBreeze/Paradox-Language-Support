package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于名字索引定义声明。
 */
class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    companion object {
        private const val VERSION = 64 //1.4.0
        private const val CACHE_SIZE = 20 * 1024 //38000+ in stellaris@3.6
    }

    override fun getKey() = ParadoxIndexManager.DefinitionNameKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
