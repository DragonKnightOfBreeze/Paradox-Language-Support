package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtDeclarationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 声明规则（declaration）。
 *
 * 概述：
 * - 描述某一可声明实体（如“游戏规则项”等）在声明处需要满足的属性结构与子类型使用情况。
 * - 由 `declaration[name] = { ... }` 或相关扩展写法声明。
 *
 * 定位：
 * - 常作为扩展规则内部的“属性形式成员”出现：例如 `game_rules`、`inline_scripts`、`parameters`、`dynamic_values` 等，当成员以属性规则形式声明时，为其生成“声明处配置”。
 * - 亦兼容直接的 `declaration[name] = { ... }` 写法（若出现）。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/internal/schema.cwt 的通用映射
 * ## replace_scopes = $scope_context
 * ## push_scope = $scope
 * ## cardinality = $cardinality
 * ## predicate = $predicate
 * $$declaration = $declaration
 * ```
 *
 * @property name 名称。
 * @property configForDeclaration 对应“声明处”的属性规则。
 * @property subtypesUsedInDeclaration 在声明中实际使用到的子类型集合。
 */
interface CwtDeclarationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String

    val configForDeclaration: CwtPropertyConfig
    val subtypesUsedInDeclaration: Set<String>

    interface Resolver {
        /** 由成员属性规则解析为声明规则；可选指定 [name] 覆盖键侧名称。*/
        fun resolve(config: CwtPropertyConfig, name: String? = null): CwtDeclarationConfig?
    }

    companion object : Resolver by CwtDeclarationConfigResolverImpl()
}
