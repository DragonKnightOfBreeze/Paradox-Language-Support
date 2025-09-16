package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedComplexEnumValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：复杂枚举值规则（complex_enum value）。
 *
 * 概述：
 * - 为复杂枚举的具体值声明类型标识与可选提示。
 * - 由 `complex_enum_value[name] = { ... }` 或相关扩展写法声明。
 *
 * @property name 名称。
 * @property type 值类型标识。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedComplexEnumValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则与 [type] 解析为“扩展的复杂枚举值规则”。*/
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig
    }

    companion object : Resolver by CwtExtendedComplexEnumValueConfigResolverImpl()
}
