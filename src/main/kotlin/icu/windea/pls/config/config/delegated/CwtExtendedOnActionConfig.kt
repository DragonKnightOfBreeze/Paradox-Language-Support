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
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `on_actions` 下的每个成员规则，解析为本规则。
 * - 可用注记：`## event_type = ...`、`## hint = ...`。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/internal/schema.cwt
 * # extended
 * on_actions = {
 *     $on_action$
 * }
 * ```
 *
 * ```cwt
 * # 来自 cwt/cwtools-stellaris-config/config/on_actions.cwt（节选）
 * on_actions = {
 *     ## event_type = scopeless
 *     on_game_start
 *     ## event_type = country
 *     on_game_start_country
 * }
 * ```
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
