package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 系统作用域规则（system_scope）。
 *
 * 概述：
 * - 声明一个“系统级作用域”的唯一 ID、其基底 ID 以及可读名称，用于在 UI 与校验中统一展示与比对。
 * - 由 `system_scope[id] = name` 与可选的 `base_id` 等条目解析而来。
 *
 * @property id 系统作用域 ID。
 * @property baseId 基底作用域 ID（用于继承/归类）。
 * @property name 可读名称。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `system_scopes` 下的每个成员属性。
 * - 规则名取自成员属性键，即 `id`，如 `Prev`、`Root`、`This`。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/system_scopes.core.cwt
 * system_scopes = {
 *     This = {}
 *     Root = {}
 *     Prev = { base_id = Prev }
 * }
 * ```
 */
interface CwtSystemScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val id: String
    @FromProperty("base_id: string")
    val baseId: String
    @FromProperty(": string")
    val name: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        /** 由成员属性规则解析为系统作用域规则。*/
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig
    }

    companion object : Resolver by CwtSystemScopeConfigResolverImpl()
}

