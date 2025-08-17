package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于类型索引定义声明。
 */
class ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    companion object {
        private const val VERSION = 72 //2.0.2
        private const val CACHE_SIZE = 2 * 1024 //180+ in stellaris@3.6
    }

    override fun getKey() = ParadoxIndexManager.DefinitionTypeKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
