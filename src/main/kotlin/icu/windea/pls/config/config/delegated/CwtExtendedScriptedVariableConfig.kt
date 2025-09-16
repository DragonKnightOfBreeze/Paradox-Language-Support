package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedScriptedVariableConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：封装变量规则（scripted_variable）。
 *
 * 概述：
 * - 为封装变量声明提示等元信息，便于在脚本中统一引用与提示。
 * - 由 `scripted_variable[name] = { ... }` 或相关扩展写法声明。
 *
 * @property name 名称。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedScriptedVariableConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为“扩展的封装变量规则”。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig?
    }

    companion object : Resolver by CwtExtendedScriptedVariableConfigResolverImpl()
}
