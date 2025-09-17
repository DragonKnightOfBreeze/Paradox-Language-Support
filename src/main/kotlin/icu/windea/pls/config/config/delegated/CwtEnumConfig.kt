package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtEnumConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 简单枚举规则。
 *
 * 用于描述拥有一组固定的可选项（即枚举值）的简单枚举。
 *
 * 路径定位：`enums/enum[{name}]`，`{name}` 匹配规则名称（枚举名）。
 *
 * CWTools 兼容性：部分兼容。PLS 仅支持常量类型（[CwtDataTypes.Constant]）的可选项。
 *
 * 示例：
 * ```cwt
 * enums = {
 *     enum[weight_or_base] = { weight base }
 * }
 * ```
 *
 * @property name 名称（枚举名）。
 * @property values 可选项集合（忽略大小写）。
 * @property valueConfigMap 可选项到对应的值规则的映射。
 */
interface CwtEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("enum[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由属性规则解析为简单枚举规则。 */
        fun resolve(config: CwtPropertyConfig): CwtEnumConfig?
    }

    companion object : Resolver by CwtEnumConfigResolverImpl()
}
