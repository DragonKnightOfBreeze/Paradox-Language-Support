package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSingleAliasConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 单别名规则：`single_alias[<name>] = { ... }`。
 *
 * - 用于定义可在使用处整体“内联”的复合结构，常见于修正（modifier）/触发（trigger）/效果（effect）等场景。
 * - 规则体内通常使用 `alias_name[...] = alias_match_left[...]` 来约束左右值的匹配类型，并可包含其它字段（如 `potential`、`description` 等）。
 * - 与 `alias[...]` 不同，`single_alias[...]` 不产生可复用的键-值别名，而是提供一段可直接展开到目标位置的规则（扩展规则）。
 *
 * 字段与方法：
 * - `name`: 单别名名（键名）。
 * - `inline(config)`: 将当前单别名的值与子规则深拷贝后“内联”到传入的 `config` 上，返回新的 `CwtPropertyConfig`。
 *
 * 典型示例：见 `cwtools-stellaris-config/config/aliases.cwt` 中
 * `single_alias[modifier_clause]`、`single_alias[triggered_modifier_clause]` 等。
 */
interface CwtSingleAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("single_alias[$]")
    val name: String

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig?
    }

    companion object : Resolver by CwtSingleAliasConfigResolverImpl()
}
