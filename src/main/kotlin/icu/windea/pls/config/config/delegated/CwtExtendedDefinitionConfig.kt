package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedDefinitionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：定义规则（definition）。
 *
 * 概述：
 * - 在常规成员规则的基础上，声明“定义”的补充信息，如类型标识与提示。
 * - 常用于把某段脚本标记为某种定义（definition）并参与导航/校验。
 *
 * @property name 名称。
 * @property type 定义类型标识（如 `scripted_trigger`）。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedDefinitionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("type: string")
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为“扩展的定义规则”。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig?
    }

    companion object : Resolver by CwtExtendedDefinitionConfigResolverImpl()
}

