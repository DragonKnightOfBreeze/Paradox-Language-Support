package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 作用域规则（scope）。
 *
 * 概述：
 * - 声明一个作用域 ID 及其别名集合，用于作用域匹配、规范化与提示。
 * - 通常在作用域管理与规则校验中使用。
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
        /** 由成员属性规则解析为作用域规则。*/
        fun resolve(config: CwtPropertyConfig): CwtScopeConfig?
    }

    companion object : Resolver by CwtScopeConfigResolverImpl()
}
