package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 内联脚本用法的索引（基于内联脚本表达式）。
 */
class ParadoxInlineScriptUsageIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = ChronicleIndexKeys.InlineScriptUsage

    override fun getVersion() = ChronicleIndexVersions.LocalisationStub
}
