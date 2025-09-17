package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtInlineConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 内联规则。
 *
 * 用于描述内联逻辑的使用处的结构，从而在脚本文件中提供代码补全、代码检查等功能。
 * 内联逻辑使得一段代码片段可以在编写时被复用。在运行时，其使用处会被替换为内联后的实际代码片段。
 * 目前仅适用于内联脚本（inline script）。
 *
 * 路径定位：`inline[{name}]`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * ## inline_script_expression = ""
 * inline[inline_script] = filepath[common/inline_scripts/,.txt]
 * ```
 *
 * @property name 名称。
 */
interface CwtInlineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("inline[$]")
    val name: String

    /** 将该内联展开为普通属性规则，供后续流程直接消费。*/
    fun inline(): CwtPropertyConfig

    interface Resolver {
        /** 由属性规则解析为内联规则。*/
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig?
    }

    companion object : Resolver by CwtInlineConfigResolverImpl()
}
