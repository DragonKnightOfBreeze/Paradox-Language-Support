package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

// TODO 2.0.4+ refine doc

/**
 * 系统作用域规则。
 *
 * 用于提供系统作用域（system scope）的相关信息（快速文档、基底 ID、可读名称）。
 *
 * 系统作用域（system scope）是一组预定义的作用域连接（scope link），用来获取或切换到需要的作用域。
 *
 * 定位：`system_scopes/{name}`，`{name}` 匹配规则名称（系统作用域 ID）。
 *
 * CWTools 兼容性：PLS 扩展。
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

