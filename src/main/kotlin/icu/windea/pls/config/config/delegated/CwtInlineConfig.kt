package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtInlineConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 内联规则。
 *
 * 用于描述内联的使用处的结构，从而在脚本文件中提供代码高亮、引用解析、代码补全、代码检查等功能。
 * 这些结构可以在脚本文件中的各种地方使用（不限于定义声明中），但是也存在特定的规则和限制。
 * 内联逻辑使得一段代码片段可以在编写时被复用。在运行时，其使用处会被替换为内联后的实际代码片段。
 *
 * 目前仅适用于**内联脚本（inline scripts）**。
 *
 * 路径定位：`inline[{name}]`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * inline[inline_script] = filepath[common/inline_scripts/,.txt]
 * ```
 *
 * @property name 名称。
 */
interface CwtInlineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("inline[$]")
    val name: String

    interface Resolver {
        /** 由属性规则解析为内联规则。*/
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig?
    }

    companion object : Resolver by CwtInlineConfigResolverImpl()
}

