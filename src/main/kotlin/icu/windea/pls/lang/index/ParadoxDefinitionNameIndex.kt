package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于名字索引定义声明。
 */
class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    companion object {
        private const val VERSION = 71 //2.0.1-dev
        private const val CACHE_SIZE = 20 * 1024 //38000+ in stellaris@3.6
    }

    override fun getKey() = ParadoxIndexManager.DefinitionNameKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE

    /**
     * 用于快速索引文本格式的名字（其定义类型为`text_format`）。它们是忽略大小写的。
     */
    class TextFormatIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
        override fun getKey() = ParadoxIndexManager.DefinitionNameForTextFormatKey
        override fun getVersion() = VERSION
    }
}
