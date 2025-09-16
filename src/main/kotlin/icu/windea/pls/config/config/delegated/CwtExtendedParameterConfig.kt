package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedParameterConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement

/**
 * 扩展：参数规则（parameters）。
 *
 * 概述：
 * - 为“参数化规则”定义其上下文键与上下文规则（单个/多个），并可选择继承调用处的上下文。
 * - 由 `parameters[name] = { ... }` 或相关扩展写法声明。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `parameters` 下的每个成员规则，解析为本规则。
 * - 可用注记：`## context_key`、`## context_configs_type`、`## inherit`。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/internal/schema.cwt
 * # extended
 * parameters = {
 *     ## context_key = $scalar
 *     ## context_configs_type = $enum:context_configs_type$
 *     ## inherit
 *     $parameter$
 *     ## context_key = $scalar
 *     ## context_configs_type = $enum:context_configs_type$
 *     ## inherit
 *     $parameter$ = $declaration
 * }
 * ```
 *
 * @property name 名称。
 * @property contextKey 上下文键（如 `scripted_trigger@X`）。
 * @property contextConfigsType 上下文规则的聚合类型（`single` 或 `multiple`）。
 * @property inherit 是否继承调用处的上下文（规则与作用域上下文）。
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
        /** 由成员规则解析为“扩展的参数规则”。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig?
    }

    companion object : Resolver by CwtExtendedParameterConfigResolverImpl()
}
