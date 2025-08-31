package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 链接规则：定义脚本作用域（或值）的跳转关系。
 *
 * - 游戏脚本：见 `config/links.cwt`，如 `owner`, `planet`, `leader` 等，将输入作用域映射到输出作用域。
 * - 本地化：见 `config/localisation_links.cwt`，如 `Owner`, `Planet` 等，专用于本地化链接。
 * - 也支持“来源于数据”的链接（from_data/from_argument），如 `event_target`、`parameter`、`variable`。
 *
 * 字段语义：
 * - `name`: 链接名（键名）。
 * - `type`: 链接类型，`scope`/`value`/`both`；若为空，视为 `scope`。
 * - `fromData`: 是否从数据源取得链接目标（如 `value[event_target]`）。
 * - `fromArgument`: 是否从参数取得链接目标（用于本地化命令参数等）。
 * - `prefix`: 目标名称前缀（如 `parameter:`），为空表示无前缀。
 * - `dataSource`: 数据源（支持 `value[...]` 或模板表达式）；解析为 [dataSourceExpression]。
 * - `inputScopes`: 允许的输入作用域集合。
 * - `outputScope`: 输出的目标作用域；若为空且 `fromData`，由数据源决定。
 * - `forDefinitionType`: 仅用于特定 definition 类型（可选）。
 *
 * 便捷方法：
 * - `forScope()`: 此链接是否适用于作用域跳转。
 * - `forValue()`: 此链接是否适用于值链接（如变量名）。
 */
interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("")
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

    fun forScope() = type != "value" /* type.isNullOrEmpty() || type == "both" || type == "scope" */
    fun forValue() = type == "both" || type == "value"

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig?
        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig?
        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig
    }

    companion object : Resolver by CwtLinkConfigResolverImpl()
}
