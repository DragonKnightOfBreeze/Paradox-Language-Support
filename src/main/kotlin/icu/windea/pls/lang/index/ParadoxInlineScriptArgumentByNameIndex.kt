package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 基于存根的索引：按传参名索引内联脚本调用处的参数（仅索引参数名）。
 */
class ParadoxInlineScriptArgumentByNameIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey(): StubIndexKey<String, ParadoxScriptProperty> = ParadoxIndexKeys.InlineScriptArgument
    override fun getVersion(): Int = 75
    override fun getCacheSize(): Int = 4 * 1024
}
