package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

// TODO 2.0.4+ refine doc

/**
 * 链接规则（link）。
 *
 * 概述：
 * - 声明“链接名称”与其数据来源、输入/输出作用域等，从而将脚本上下文（作用域/值）与目标实体建立关联，用于跳转、补全与校验（如 `event_target`、`var`）。
 * - 由 `link[name] = { ... }` 声明；当用于本地化上下文时会生成对应的本地化变体。
 * - 本规则的 [configExpression] 与 [dataSourceExpression] 一致（即数据来源表达式）。
 *
 * 路径定位：`links/\$`、`localisation_links/\$`
 * - `\$`：链接名。
 *
 * 示例（通用示意）：
 *     links = {
 *         event_target = {
 *             input_scopes = { any }
 *             output_scope = any
 *             data_source = scope
 *         }
 *     }
 *
 * @property name 链接名。
 * @property type 链接类型（`scope`/`value`/`both` 等，缺省按 `scope` 处理）。
 * @property fromData 是否从数据环境中读取（`from_data`）。
 * @property fromArgument 是否从参数中读取（`from_argument`）。
 * @property prefix 前缀（如 `prev`/`root` 等）。
 * @property dataSource 数据源标识（可为表达式，见 [dataSourceExpression]）。
 * @property inputScopes 输入作用域集合（`input_scopes`）。
 * @property outputScope 输出作用域（`output_scope`）。
 * @property forDefinitionType 仅用于指定的定义类型。
 * @property forDefinitionType 仅用于指定的定义类型。
 * @property forLocalisation （计算属性）是否为本地化上下文的变体。
 * @property dataSourceExpression （计算属性）数据源对应的规则表达式。
 */
interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("type: string?")
    val type: String?
    @FromProperty("from_data: boolean", defaultValue = "false")
    val fromData: Boolean
    @FromProperty("from_argument: boolean", defaultValue = "false")
    val fromArgument: Boolean
    @FromProperty("prefix: string?")
    val prefix: String?
    @FromProperty("data_source: string?")
    val dataSource: String?
    @FromProperty("input_scopes: string[]")
    val inputScopes: Set<String>
    @FromProperty("output_scope: string?")
    val outputScope: String?
    @FromProperty("for_definition_type: string?")
    val forDefinitionType: String?

    val forLocalisation: Boolean
    val dataSourceExpression: CwtDataExpression?

    override val configExpression: CwtDataExpression? get() = dataSourceExpression

    // type = null -> default to "scope"
    // output_scope = null -> transfer scope based on data source
    // e.g., for event_target, output_scope should be null

    /** 是否用于“作用域”链接（含缺省情形）。*/
    fun forScope() = type != "value" /* type.isNullOrEmpty() || type == "both" || type == "scope" */
    /** 是否用于“值”链接。*/
    fun forValue() = type == "both" || type == "value"

    interface Resolver {
        /** 由 `link[...]` 的属性规则解析为链接规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig?
        /** 从 `link[...]` 解析本地化上下文的链接规则变体。*/
        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig?
        /** 基于已有链接规则生成本地化上下文的变体。*/
        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig
    }

    companion object : Resolver by CwtLinkConfigResolverImpl()
}
