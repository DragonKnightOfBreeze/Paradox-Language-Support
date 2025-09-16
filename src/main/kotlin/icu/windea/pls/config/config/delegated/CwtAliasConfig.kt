package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtAliasConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 别名规则（alias）。
 *
 * 概述：
 * - 为触发（trigger）/效应（effect）等提供“别名名-子名”到真实规则的映射与约束，提升可读性与复用性。
 * - 由 `alias[name:subName] = { ... }` 声明。
 * - 本规则的 [configExpression] 等同于 [subNameExpression]。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 的顶层 `else` 分支中处理未匹配的键。
 * - 当键形如 `alias[...]` 时，尝试解析为本规则；`name` 与 `subName` 由 `alias[<name>:<subName>]` 的方括号内容按冒号拆分得到。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/cwtools-stellaris-config/config/aliases.cwt
 * alias[name:name] = localisation
 * alias[modifier:<modifier>] = float
 * ```
 *
 * @property name 别名名（`alias[$:*]`）。
 * @property subName 子名（`alias[*:$]`）。
 * @property supportedScopes 允许的作用域集合（`## scope/scopes`）。
 * @property outputScope 推入/输出的作用域（`## push_scope`）。
 * @property subNameExpression 子名对应的规则表达式。
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

    /** 将别名内联为普通属性规则，便于下游流程直接消费。*/
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        /** 由 `alias[...]` 的属性规则解析为别名规则。*/
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig?
    }

    companion object : Resolver by CwtAliasConfigResolverImpl()
}
