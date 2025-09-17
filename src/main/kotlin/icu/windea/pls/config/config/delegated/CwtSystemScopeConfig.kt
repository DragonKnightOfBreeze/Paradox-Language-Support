package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxScope
import icu.windea.pls.model.ParadoxScopeContext

/**
 * 系统作用域规则。
 *
 * 用于提供系统作用域（system scope）的相关信息（快速文档、基底 ID、可读名称）。
 *
 * **系统作用域（system scope）** 是一组预定义的 **作用域连接（scope link）**，用于获取或切换到需要的作用域。
 *
 * 路径定位：`system_scopes/{name}`，`{name}` 匹配规则名称（系统作用域 ID）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * system_scopes = {
 *     This = {}
 *     Root = {}
 *     Prev = { base_id = Prev }
 *     From = { base_id = From }
 *     # ...
 * }
 * ```
 *
 * @property id 系统作用域 ID。
 * @property baseId 基底作用域 ID（用于继承/归类）。
 * @property name 可读名称。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
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
        /** 由属性规则解析为系统作用域规则。*/
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig
    }

    companion object : Resolver by CwtSystemScopeConfigResolverImpl()
}

