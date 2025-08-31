package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtModifierCategoryConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 修正类别规则：描述修正（modifier）所属的类别及其可用作用域。
 *
 * - 供修正规则（扩展规则）引用，用于确定某修正在哪些脚本作用域下有效与可提示。
 * - 类别名通常用于 UI 展示与过滤。
 *
 * 字段：
 * - `name`: 类别名。
 * - `supportedScopes`: 该类别支持的脚本作用域集合。
 */
interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("supported_scopes: string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig?
    }

    companion object : Resolver by CwtModifierCategoryConfigResolverImpl()
}
