package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本的传入参数的索引（基于内联脚本表达式）。
 */
class ParadoxInlineScriptArgumentIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = PlsIndexKeys.InlineScriptArgument

    override fun getVersion() = PlsIndexVersions.LocalisationStub
}
