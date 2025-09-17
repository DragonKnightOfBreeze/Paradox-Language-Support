package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxScope
import icu.windea.pls.model.ParadoxScopeContext

/**
 * 作用域规则。
 *
 * 用于提供作用域类型（scope type）的相关信息（快速文档、别名）。
 *
 * 路径定位：`scopes/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * scopes = {
 *     Country = { aliases = { country } }
 * }
 * ```
 *
 * @property name 规则名称。
 * @property aliases 该作用域的别名集合（大小写不敏感）。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
 */
interface CwtScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("aliases: string[]")
    val aliases: Set<@CaseInsensitive String>

    interface Resolver {
        /** 由属性规则解析为作用域规则。*/
        fun resolve(config: CwtPropertyConfig): CwtScopeConfig?
    }

    companion object : Resolver by CwtScopeConfigResolverImpl()
}
