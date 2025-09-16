package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocalisationCommandConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 本地化命令规则（localisation command）。
 *
 * 概述：
 * - 声明某个本地化命令及其允许使用的作用域集合，便于在本地化脚本中进行校验与提示。
 * - 由 `localisation_command[name] = { ... }` 或相关扩展写法声明。
 *
 * @property name 命令名称。
 * @property supportedScopes 允许的作用域集合。
 */
interface CwtLocalisationCommandConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由成员属性规则解析为本地化命令规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig
    }

    companion object : Resolver by CwtLocalisationCommandConfigResolverImpl()
}
