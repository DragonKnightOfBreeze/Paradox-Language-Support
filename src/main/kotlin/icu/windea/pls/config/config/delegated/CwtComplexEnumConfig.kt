package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtComplexEnumConfigResolverImpl

/**
 * 复杂枚举规则：`complex_enum[<name>] = { ... }`。
 *
 * - 与普通 `enum[...]` 不同，复杂枚举的取值不是静态列出，而是通过在若干文件/规则中按“锚点”收集生成。
 * - 可配合路径匹配字段（见 [CwtFilePathMatchableConfig]）将作用范围限制到特定目录/文件。
 * - `searchScopeType` 用于限定“等价性”的查找作用域（如 `definition`：仅同一 definition 下视为同一复杂枚举值）。
 * - `nameConfig` 指定如何从规则中解析值名；其中的 `enum_name` 成员作为锚点，见 [enumNameConfigs]。
 *
 * PLS 在索引阶段依据这些锚点聚合可选值，并为补全/跳转提供来源追踪。
 *
 * @property searchScopeType 查询作用域，认为仅该作用域下的复杂枚举值是等同的。（目前支持：definition）
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
