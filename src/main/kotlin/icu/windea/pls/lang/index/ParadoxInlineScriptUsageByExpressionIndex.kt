package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 基于表达式（inline_script 的路径表达式）索引内联脚本的使用位置。
 */
class ParadoxInlineScriptUsageByExpressionIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = ParadoxIndexKeys.InlineScriptUsageByExpression

    // 与其它索引保持一致版本（如需强制重建索引，可在发布时统一提升版本）
    override fun getVersion() = 75 // VERSION for 2.0.5

    override fun getCacheSize() = 4 * 1024
}
