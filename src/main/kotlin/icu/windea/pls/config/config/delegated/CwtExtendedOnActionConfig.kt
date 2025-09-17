package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedOnActionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * on action 的扩展规则。
 *
 * 用于为对应的 on action 提供额外的提示信息（如文档注释、内嵌提示），以及指定事件类型。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 * - on action 即类型为 `on_action` 的定义。
 * - 事件类型是通过 `## event_type` 选项指定的。这会重载声明规则中的所有对事件的引用为对该类型事件的引用。
 *
 * 路径定位：`on_actions/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：不兼容，拥有不同的格式与行为。
 *
 * 示例：
 * ```cwt
 * on_actions = {
 *     ### Some documentation
 *     ## hint = §RSome hint text§!
 *     ## replace_scopes = { this = country root = country }
 *     ## event_type = country
 *     x
 * }
 * ```
 *
 * @property name 名称。
 * @property eventType 事件类型。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedOnActionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("event_type: string")
    val eventType: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为 on action 的扩展规则。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig?
    }

    companion object : Resolver by CwtExtendedOnActionConfigResolverImpl()
}
