package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedOnActionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：on_action 规则。
 *
 * 概述：
 * - 为 on_action 声明其事件类型（如 `country`、`system`），并可附带提示信息。
 * - 由 `on_action[name] = { ... }` 或相关扩展写法声明。
 *
 * @property name 名称。
 * @property eventType 事件类型（如 `country`、`system`）。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedOnActionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("event_type: string")
    val eventType: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为“扩展的 on_action 规则”。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig?
    }

    companion object : Resolver by CwtExtendedOnActionConfigResolverImpl()
}
