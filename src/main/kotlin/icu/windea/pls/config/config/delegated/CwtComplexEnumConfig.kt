package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtComplexEnumConfigResolverImpl

/**
 * 复杂枚举规则（complex_enum[...]）。
 *
 * 概述：
 * - 用于描述基于“锚点（enum_name）”聚合的复杂枚举集合，支持从根或从当前上下文开始查询。
 * - 由 `complex_enum[name] = { ... }` 声明。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，顶层键 `enums` 的每个成员既可能解析为 `enum[...]`，也可能解析为 `complex_enum[...]`。
 * - 名称从成员属性键中提取：去除前后缀 `complex_enum[` 与 `]`，得到 `name`。
 *
 * 例：
 * ```cwt
 * # 模式参考：cwt/core/internal/schema.cwt
 * enums = {
 *     complex_enum[my_complex] = {
 *         start_from_root = yes
 *         name = <definition_type_expression>
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property startFromRoot 是否从根开始查询（默认 false）。
 * @property searchScopeType 查询作用域类型，用于控制查询对象的等效性（目前支持：`definition`）。
 * @property nameConfig `name` 对应的规则。
 * @property enumNameConfigs 在 [nameConfig] 中作为锚点的 `enum_name` 对应的规则集合。
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
        /** 由 `complex_enum[...]` 的属性规则解析为复杂枚举规则。*/
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig?
    }

    companion object : Resolver by CwtComplexEnumConfigResolverImpl()
}
