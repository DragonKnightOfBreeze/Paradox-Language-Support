package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedParameterConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement

/**
 * 参数的扩展规则。
 *
 * 用于为对应的参数（parameter）提供额外的提示信息（如文档注释），以及指定规则上下文与作用域上下文。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 * - 参数是指触发（trigger）、效应（effect）或内联脚本（inline script）的参数，格式为 `$PARAM$` 或 `$PARAM|DEFAULT_VALUE$`。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：`parameters/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * parameters = {
 *     ## replace_scopes = { this = country root = country }
 *     ## context_key = some_trigger
 *     PARAM
 *     ## context_configs_type = multiple
 *     ## context_key = some_trigger
 *     PARAM = { ... }
 *     ## context_configs_type = multiple
 *     ## context_key = some_trigger
 *     PARAM = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * @property name 名称。
 * @property contextKey 上下文键（如 `scripted_trigger@X`）。
 * @property contextConfigsType 上下文规则的聚合类型（`single` 或 `multiple`）。
 * @property inherit 是否继承使用处的规则上下文与作用域上下文。
 *
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
 */
interface CwtExtendedParameterConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_key: string")
    val contextKey: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String
    @FromOption("inherit", defaultValue = "no")
    val inherit: Boolean

    /** 得到处理后的“上下文规则容器”。*/
    fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*>

    /** 得到由其声明的上下文规则列表。*/
    fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由成员规则解析为参数的扩展规则。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig?
    }

    companion object : Resolver by CwtExtendedParameterConfigResolverImpl()
}
