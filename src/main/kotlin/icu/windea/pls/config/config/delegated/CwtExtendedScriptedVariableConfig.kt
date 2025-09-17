package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedScriptedVariableConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 封装变量的扩展规则。
 *
 * 用于为对应的封装变量（scripted variable）提供额外的提示信息（如文档注释、内嵌提示）。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 *
 * 路径定位：`scripted_variables/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * scripted_variables = {
 *     ### Some documentation
 *     ## hint = §RSome hint text§!
 *     x # or `x = xxx`
 * }
 * ```
 *
 * @property name 名称。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedScriptedVariableConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为封装变量的扩展规则。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedScriptedVariableConfig?
    }

    companion object : Resolver by CwtExtendedScriptedVariableConfigResolverImpl()
}
