package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 基于内联脚本表达式索引内联脚本使用。
 */
class ParadoxInlineScriptUsageIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = ParadoxIndexKeys.InlineScriptUsage

    override fun getVersion() = 76 // VERSION for 2.0.6
}
