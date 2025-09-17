package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedComplexEnumValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 复杂枚举值的扩展规则。
 *
 * 用于为对应的复杂枚举值提供额外的提示信息（如文档注释、内嵌提示）。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 *
 * 路径定位：`complex_enum_values/{type}/{name}`，`{type}` 匹配枚举名，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * complex_enum_values = {
 *     component_tag = {
 *         ### Some documentation
 *         ## hint = §RSome hint text§!
 *         x # or `x = xxx`
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property type 枚举名。
 * @property hint 提示文本（可选）。
 */
interface CwtExtendedComplexEnumValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为复杂枚举值的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig
    }

    companion object : Resolver by CwtExtendedComplexEnumValueConfigResolverImpl()
}
