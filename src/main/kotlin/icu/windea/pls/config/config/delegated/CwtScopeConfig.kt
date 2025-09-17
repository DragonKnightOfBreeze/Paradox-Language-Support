package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

// TODO 2.0.4+ refine doc

/**
 * 作用域规则。
 *
 * 用于提供作用域（scope）的相关信息（快速文档、别名）。
 *
 * 路径定位：`scopes/{name}`，`{name}` 匹配规则名称（作用域 ID）。
 *
 * 示例：
 * ```cwt
 * scopes = {
 *     Country = { aliases = { country } }
 * }
 * ```
 *
 * @property name 作用域 ID。
 * @property aliases 该作用域的别名集合（大小写不敏感）。
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
