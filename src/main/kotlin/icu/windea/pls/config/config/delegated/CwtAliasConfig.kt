package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtAliasConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 别名规则：`alias[<name>:<sub_name>] = <config>`。
 *
 * - 在 CWT 规则文件中，别名用于复用一段属性规则，常见于事件、修正、触发器等模块。
 * - `name` 表示别名组名，`sub_name` 表示具体别名项；其右侧的 `<config>` 为被复用的属性规则体。
 * - 在 PLS 中，别名会通过 [inline] 展开为一个新的 [CwtPropertyConfig]，并以 `subName` 作为新键。
 * - `supportedScopes`/`outputScope` 用于限定/推断脚本作用域（script scope），影响补全与校验。
 * - [configExpression] 指向 `sub_name` 的数据表达式，便于在索引与重命名等场景进行解析。
 *
 * 典型示例：
 * - `alias[trigger:has_ethic] = { ... }`，在脚本中可写作 `has_ethic = ...`。
 *
 * 本接口为委托规则（扩展规则），其 [pointer]/[configGroup] 由底层的 [config] 转发。
 */
interface CwtAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("alias[$:*]")
    val name: String
    @FromKey("alias[*:$]")
    val subName: String
    @FromOption("scope/scopes: string | string[]")
    val supportedScopes: Set<String>
    @FromOption("push_scope: string?")
    val outputScope: String?

    val subNameExpression: CwtDataExpression

    override val configExpression: CwtDataExpression get() = subNameExpression

    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig?
    }

    companion object : Resolver by CwtAliasConfigResolverImpl()
}
