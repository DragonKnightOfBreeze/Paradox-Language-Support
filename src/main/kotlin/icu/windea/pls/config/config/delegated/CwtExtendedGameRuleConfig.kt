package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedGameRuleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：游戏规则（game_rule）。
 *
 * 概述：
 * - 为“游戏规则”声明其在声明处可用的属性结构与提示信息。
 * - 由 `game_rule[name] = { ... }` 或相关扩展写法声明。
 *
 * @property name 名称（模板表达式）。
 * @property hint 额外提示信息（可选）。
 * @property configForDeclaration 对应“声明处”的属性规则（若存在）。
 */
interface CwtExtendedGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("hint: string?")
    val hint: String?

    val configForDeclaration: CwtPropertyConfig?

    interface Resolver {
        /** 由成员规则解析为“扩展的游戏规则”。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig
    }

    companion object : Resolver by CwtExtendedGameRuleConfigResolverImpl()
}
