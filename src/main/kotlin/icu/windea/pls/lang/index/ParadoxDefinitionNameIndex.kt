package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 基于名字索引定义声明。
 */
class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    override fun getKey() = ParadoxIndexKeys.DefinitionName

    override fun getVersion() = 75 // VERSION for 2.0.5

    override fun getCacheSize() = 12 * 1024 // CACHE SIZE - 38000+ in stellaris@3.6

    /**
     * 用于快速索引文本格式的名字（其定义类型为`text_format`）。它们是忽略大小写的。
     */
    class TextFormatIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
        override fun getKey() = ParadoxIndexKeys.DefinitionNameForTextFormat
        override fun getVersion() = 75 // VERSION for 2.0.5
    }
}
