package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedDynamicValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：动态值规则（dynamic_value）。
 *
 * 概述：
 * - 为动态值声明类型标识与可选提示，便于在补全/校验时区分不同动态值族。
 * - 由 `dynamic_value[name] = { ... }` 或相关扩展写法声明。
 *
 * @property name 名称。
 * @property type 动态值类型标识。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedDynamicValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则与 [type] 解析为“扩展的动态值规则”。*/
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig
    }

    companion object : Resolver by CwtExtendedDynamicValueConfigResolverImpl()
}
