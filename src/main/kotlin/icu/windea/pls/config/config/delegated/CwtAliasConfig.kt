package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtAliasConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 别名规则。
 *
 * 别名规则是一种可以按一对多的形式，在多个位置复用（作为属性）的规则。
 * 别名规则可用来简化规则文件，提升可读性和复用性。
 * 另外，包括触发（trigger）、效应（effect）在内的多种概念对应的规则，都是以别名规则的形式提供的。
 *
 * 路径定位：`alias[{name}:{subName}]`，`{name}` 匹配名称，`{subName}`匹配子名（受限支持的数据表达式）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * # declaration
 * alias[effect:some_effect] = { ... }
 * alias[effect:some_other_effect] = { ... }
 *
 * # usage
 * scripted_effect = {
 *     alias_name[effect] = alias_match_left[effect]
 * }
 * ```
 *
 * @property name 名称。
 * @property subName 子名（受限支持的数据表达式）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 * @property outputScope 输出的作用域。
 * @property subNameExpression 子名对应的数据表达式。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [subNameExpression]）。
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
        /** 由属性规则解析为别名规则。*/
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig?
    }

    companion object : Resolver by CwtAliasConfigResolverImpl()
}
