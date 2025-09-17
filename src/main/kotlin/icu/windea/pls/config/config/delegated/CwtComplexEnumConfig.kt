package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtComplexEnumConfigResolverImpl

/**
 * 复杂枚举规则。
 *
 * 用于描述需要基于锚点动态定位可选项（即枚举值）的复杂枚举。
 * 按照路径模式匹配脚本文件，并在其中进一步匹配锚点。
 *
 * 路径定位：`enums/complex_enum[{name}]`，`{name}` 匹配规则名称（枚举名）。
 *
 * CWTools 兼容性：兼容，但存在一定的扩展。
 *
 * 示例：
 * ```cwt
 * enums = {
 *     complex_enum[component_tag] = {
 *         path = "game/common/component_tags"
 *         name = {
 *         	enum_name
 *         }
 *         start_from_root = yes
 *     }
 * }
 * ```
 *
 * @property name 名称（枚举名）。
 * @property startFromRoot 是否从文件顶部而非顶级属性开始查询（默认 false）。
 * @property searchScopeType （扩展）查询作用域类型。用于控制查询对象的等效性，认为仅该作用域下的复杂枚举值是等效的（目前仅支持：`definition`）。
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
        /** 由属性规则解析为复杂枚举规则。*/
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig?
    }

    companion object : Resolver by CwtComplexEnumConfigResolverImpl()
}
