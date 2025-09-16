package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtComplexEnumConfigResolverImpl

/**
 * @property searchScopeType 查询作用域类型。用于控制查询对象的等效性，认为仅该作用域下的复杂枚举值是等效的（目前仅支持：definition）。
 * @property nameConfig `name` 对应的 CWT 规则。
 * @property enumNameConfigs [nameConfig] 中作为锚点的 `enum_name` 对应的 CWT 规则。
 */
interface CwtComplexEnumConfig : CwtFilePathMatchableConfig {
    @FromKey("complex_enum[$]")
    val name: String
    @FromProperty("start_from_root: boolean", defaultValue = "false")
    val startFromRoot: Boolean
    @FromProperty("search_scope_type: string?")
    val searchScopeType: String?

    val nameConfig: CwtPropertyConfig
    val enumNameConfigs: List<CwtMemberConfig<*>>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig?
    }

    companion object : Resolver by CwtComplexEnumConfigResolverImpl()
}
