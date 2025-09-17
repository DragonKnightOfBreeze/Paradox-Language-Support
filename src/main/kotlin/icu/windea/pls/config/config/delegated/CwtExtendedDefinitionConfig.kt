package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedDefinitionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 定义的扩展规则。
 *
 * 用于为对应的定义提供额外的提示信息（如文档注释、内嵌提示），以及指定作用域上下文（如果支持）。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：`definitions/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * definitions = {
 *     ### Some documentation
 *     ## hint = §RSome hint text§!
 *     ## replace_scopes = { this = country root = country }
 *     ## type = scripted_trigger
 *     x # or `x = xxx`
 * }
 * ```
 *
 * @property name 名称。
 * @property type 定义类型。
 * @property hint 提示文本（可选）。
 *
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
 */
interface CwtExtendedDefinitionConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("type: string")
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为定义的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig?
    }

    companion object : Resolver by CwtExtendedDefinitionConfigResolverImpl()
}

