package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedInlineScriptConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 内联脚本的扩展规则。
 *
 * 用于为对应的内联脚本（inline script）指定规则上下文与作用域上下文。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 * - 内联脚本文件是指扩展名为 `.txt`，位于 `common/inline_scripts` 目录下的脚本文件。
 *   这些脚本可以在其他脚本文件中（几乎任意位置）被调用。
 * - 名为 `x/y` 的规则会匹配路径为 `common/inline_scripts/x/y.txt` 的内联脚本文件。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：`inline_scripts/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * inline_scripts = {
 *     ## replace_scopes = { this = country root = country }
 *     triggers/some_trigger_snippet
 *     ## context_configs_type = multiple
 *     triggers/some_trigger_snippet = { ... }
 *     ## context_configs_type = multiple
 *     triggers/some_trigger_snippet = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * @property name 名称。
 * @property contextConfigsType 上下文规则的聚合类型（`single` 或 `multiple`）。
 *
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
 */
interface CwtExtendedInlineScriptConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String // TODO 2.0.4+ 需要详细说明这个属性的用处与行为

    /** 得到处理后的“上下文规则容器”。*/
    fun getContainerConfig(): CwtMemberConfig<*>

    /** 得到由其声明的上下文规则列表。*/
    fun getContextConfigs(): List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由成员规则解析为内联脚本的扩展规则。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig
    }

    companion object : Resolver by CwtExtendedInlineScriptConfigResolverImpl()
}
