package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedDynamicValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 扩展：动态值规则（dynamic_value）。
 *
 * 概述：
 * - 为动态值声明类型标识与可选提示，便于在补全/校验时区分不同动态值族。
 * - 由 `dynamic_value[name] = { ... }` 或相关扩展写法声明。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `dynamic_values` 下的成员规则，配合“类型键”解析为本规则。
 * - 解析器通过 `resolve(config, type)` 接收外部提供的 `type`（即 `$dynamic_value_type$`）。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/internal/schema.cwt
 * # extended
 * dynamic_values = {
 *     $dynamic_value_type$ = {
 *         ## hint = $scalar
 *         $dynamic_value$
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property type 动态值类型标识。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedDynamicValueConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则与 [type] 解析为“扩展的动态值规则”。*/
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig
    }

    companion object : Resolver by CwtExtendedDynamicValueConfigResolverImpl()
}
