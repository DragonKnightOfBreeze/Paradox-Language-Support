package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSingleAliasConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 单别名规则（single_alias[...]）。
 *
 * 概述：
 * - 定义一个只包含“名称”的简单别名，常用于将某个名称直接映射为一段可复用规则结构。
 * - 由 `single_alias[name] = { ... }` 声明。
 *
 * @property name 别名名称。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 的顶层 `else` 分支中处理未匹配的键。
 * - 当键形如 `single_alias[...]` 时，解析为本规则；`name` 取自方括号中的标识。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/internal/schema.cwt
 * single_alias[$single_alias$] = $declaration
 * ```
 */
interface CwtSingleAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("single_alias[$]")
    val name: String

    /** 将该单别名内联展开为普通属性规则。*/
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        /** 由 `single_alias[...]` 的属性规则解析为单别名规则。*/
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig?
    }

    companion object : Resolver by CwtSingleAliasConfigResolverImpl()
}
