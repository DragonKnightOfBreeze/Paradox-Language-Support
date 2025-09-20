package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.expression.ParadoxCommandExpression
import icu.windea.pls.lang.expression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.expression.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.ParadoxVariableFieldExpression

/**
 * 链接规则。
 *
 * 用于描述部分复杂表达式中的部分节点的格式，并为其指定允许的作用域类型。
 * 这些节点被称为 **链接（link）**，它们通常用于切换作用域或取值，可以链式调用（如 `x.y.z`）。
 * 可参见：`scopes.log`。
 *
 * 链接可以以静态（静态链接）或动态（动态链接）两种方式声明。具体而言，动态链接可以：
 * - 整个文本作为动态数据（如 `var`）。
 * - 带前缀（如 `modifier:x`）。
 * - 使用函数调用形式（如 `relations(x)`）。
 * - 使用更复杂的函数调用形式。注意，PLS 尚未提供完整支持。
 *
 * 以下是一些常见的链接形式：
 * - **作用域链接（scope link）** - 如 `owner`。
 * - **系统作用域（system scope）** - 如 `root`。
 * - **本地化命令字段（localisation command field）** - 如 `GetName`。
 * - **事件对象引用（event target reference）** - 如 `event_target:x`。
 * - **动态数据引用（dynamic data reference）** - 如 `modifier:x` `value:x`。
 *
 * 在语义与格式上，它们类似编程语言中的函数、属性或字段。
 *
 * 路径定位：
 * 1. 常规链接：`links/{name}`，`{name}` 匹配规则名称（链接名）。
 * 2. 本地化链接：`localisation_links/{name}`，`{name}` 匹配规则名称（链接名）。
 * 3. 如果静态的本地化链接未被声明，静态的常规链接会被全部复制作为本地化链接。
 *
 * CWTools 兼容性：兼容，但存在一定的扩展。
 *
 * 示例：
 * ```cwt
 * links = {
 *     event_target = {
 *         input_scopes = { any }
 *         output_scope = any
 *     }
 *     # ...
 * }
 * ```
 *
 * @property name 规则名称（链接名）。
 * @property type 链接类型（`scope`/`value`/`both`，默认为 `scope`）。
 * @property fromData 为动态链接时，是否从数据中读取动态数据。对应的节点格式形如 `prefix:data`。
 * @property fromArgument （PLS 扩展）为动态链接时，是否从传入参数中读取动态数据。对应的节点格式形如 `func(arg)`。
 * @property prefix 为动态链接时，携带的前缀。如果为 null，则将整个文本作为动态数据。
 * @property dataSource 数据源（数据表达式）。如果为 null，则将链接视为静态的。
 * @property inputScopes 输入作用域（类型）的集合。
 * @property outputScope 输出作用域（类型）。
 * @property forDefinitionType 仅用于指定的定义类型。
 * @property forLocalisation 是否为本地化链接（在本地化文件而非脚本文件中使用）。
 * @property dataSourceExpression 数据源对应的数据表达式。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [dataSourceExpression]）。
 *
 * @see ParadoxScopeFieldExpression
 * @see ParadoxValueFieldExpression
 * @see ParadoxVariableFieldExpression
 * @see ParadoxCommandExpression
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
        /** 由属性规则解析为（常规）链接规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig?
        /** 由属性规则解析为本地化链接规则。 */
        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig?
        /** 由已有的（常规）链接规则，解析为本地化链接规则。*/
        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig
    }

    companion object : Resolver by CwtLinkConfigResolverImpl()
}
