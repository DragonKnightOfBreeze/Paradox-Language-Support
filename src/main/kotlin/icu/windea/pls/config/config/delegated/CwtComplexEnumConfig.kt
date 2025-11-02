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
 * @property startFromRoot 是否从文件顶部（而非顶级属性）开始查询。
 * @property perDefinition （PLS 扩展）是否将同名同类型的复杂枚举值的等效性限制在定义级别（而非文件级别）。
 * @property nameConfig `name` 对应的规则。
 * @property enumNameConfigs 在 [nameConfig] 中作为锚点的 `enum_name` 对应的规则集合。
 */
interface CwtComplexEnumConfig : CwtFilePathMatchableConfig {
    @FromKey("complex_enum[$]")
    val name: String
    @FromProperty("start_from_root: boolean", defaultValue = "no")
    val startFromRoot: Boolean
    @FromProperty("per_definition: boolean", defaultValue = "no")
    val perDefinition: Boolean

    val searchScopeType: String?
    val nameConfig: CwtPropertyConfig
    val enumNameConfigs: List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由属性规则解析为复杂枚举规则。*/
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig?
    }

    companion object : Resolver by CwtComplexEnumConfigResolverImpl()
}
