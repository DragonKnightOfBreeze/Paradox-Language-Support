package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedInlineScriptConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：内联脚本规则（inline_script）。
 *
 * 概述：
 * - 为内联脚本（inline_script）定义其“上下文规则”的声明与聚合方式（单个/多个）。
 * - 由 `inline_script[name] = { ... }` 声明。
 *
 * @property name 名称。
 * @property contextConfigsType 上下文规则的聚合类型（`single` 或 `multiple`）。
 */
interface CwtExtendedInlineScriptConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String

    /** 得到处理后的“上下文规则容器”。*/
    fun getContainerConfig(): CwtMemberConfig<*>

    /** 得到由其声明的上下文规则列表。*/
    fun getContextConfigs(): List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由 `inline_script[...]` 的成员规则解析为扩展规则。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig
    }

    companion object : Resolver by CwtExtendedInlineScriptConfigResolverImpl()
}
