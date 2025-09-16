package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 链接规则（link）。
 *
 * 概述：
 * - 将“数据源/作用域/值”等上下文与目标内容建立关联，用于解析跳转、补全与校验（如 event_target、var 等）。
 * - 由 `link[name] = { ... }` 声明；当用于本地化时会生成本地化专用变体。
 * - 本规则的 [configExpression] 通常等同于 [dataSourceExpression]。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中：
 *   - 读取顶层键 `links` 下的每个成员属性，调用 `resolve`，并按 `name` 存入 `configGroup.links`。
 *   - 读取顶层键 `localisation_links` 下的每个成员属性，调用 `resolveForLocalisation`，并按 `name` 存入 `configGroup.localisationLinks`。
 * - 规则名取自成员属性键，即 `name`。
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
