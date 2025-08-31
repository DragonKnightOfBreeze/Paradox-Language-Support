package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 系统作用域规则：描述游戏/脚本中的内置“系统作用域”（system scope），用于校验与提示作用域跳转。
 *
 * - 常见如 `country`、`planet` 等，通常作为作用域链的起点或中间节点。
 * - 解析器可能在缺省时回退到 `id` 作为 `baseId`/`name`。
 *
 * 字段：
 * - `id`: 作用域 ID。
 * - `baseId`: 基础作用域 ID，可用于继承或兼容处理。
 * - `name`: 展示名称（本地化友好名）。
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
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig
    }

    companion object : Resolver by CwtSystemScopeConfigResolverImpl()
}

