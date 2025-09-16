package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtInlineConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 内联规则（inline）。
 *
 * 概述：
 * - 将一段可复用的属性结构以“内联”的方式注入到使用处，减少重复书写。
 * - 由 `inline[name] = { ... }` 声明。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 的顶层 `else` 分支中处理未匹配的键。
 * - 当键形如 `inline[...]` 时，解析为本规则；`name` 取自方括号中的标识。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/internal/schema.cwt
 * inline[$inline$] = $declaration
 * ```
 *
 * @property name 内联名称。
 */
interface CwtInlineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("inline[$]")
    val name: String

    /** 将该内联展开为普通属性规则，供后续流程直接消费。*/
    fun inline(): CwtPropertyConfig

    interface Resolver {
        /** 由 `inline[...]` 的属性规则解析为内联规则。*/
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig?
    }

    companion object : Resolver by CwtInlineConfigResolverImpl()
}
