package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtDynamicValueTypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 动态值类型规则。
 *
 * 用于为对应的动态值类型提供预定义（硬编码）的动态值。
 * 动态值是一组不固定的可选项，通常是合法的标识符，使用同名本地化的文本作为 UI 显示。
 * 事件目标（event target）、变量（variable）、标志（flag）等通常都会被视为动态值。
 *
 * CWTools 兼容性：部分兼容。PLS 仅支持常量类型（[CwtDataTypes.Constant]）的可选项。
 *
 * 路径定位：`values/value[{name}]`，`{name}` 匹配规则名称（动态值类型）。
 *
 * 示例：
 * ```cwt
 * values = {
 *     value[event_target] = { owner }
 * }
 * ```
 *
 * @property name 名称（动态值类型）。
 * @property values 可选项集合（忽略大小写）。
 * @property valueConfigMap 可选项到对应的值规则的映射。
 */
interface CwtDynamicValueTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("value[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由属性规则解析为动态值类型规则。 */
        fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig?
    }

    companion object : Resolver by CwtDynamicValueTypeConfigResolverImpl()
}
