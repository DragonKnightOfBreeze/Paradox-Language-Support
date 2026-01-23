package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression

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
 * CWTools 兼容性：兼容，但存在一些扩展。
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
 * @property fromData 为动态链接时，是否从后置数据中读取动态数据。对应的节点格式形如 `prefix:data`。
 * @property fromArgument （PLS 扩展）为动态链接时，是否从传参中读取动态数据。对应的节点格式形如 `func(arg)`。
 * @property argumentSeparator （PLS 扩展） 为动态链接且有多个传参时，使用的传参分隔符（`comma`/`pipe`，默认为 `comma`）。
 * @property prefix 为动态链接时，携带的前缀。如果为 `null`，则将整个文本作为动态数据。
 * @property dataSources 数据源（数据表达式）。如果有多个传参，则可以有多个。如果为空，则将链接视为静态链接。
 * @property inputScopes 输入作用域（类型）的集合。
 * @property outputScope 输出作用域（类型）。如果为 `null`，则表示需要基于数据源传递作用域。
 * @property forDefinitionType 仅用于指定的定义类型。
 * @property isLocalisationLink 是否为本地化链接（可在本地化命令中使用）。
 * @property dataSourceIndex 数据源的索引。适用于有多个传参的场合。
 * @property dataSourceExpression 指定索引（[dataSourceIndex]）的数据源对应的数据表达式。
 * @property dataSourceExpressions 数据源对应的一组数据表达式。
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
    val type: CwtLinkType
    @FromProperty("from_data: boolean", defaultValue = "no")
    val fromData: Boolean
    @FromProperty("from_argument: boolean", defaultValue = "no")
    val fromArgument: Boolean
    @FromProperty("argument_separator: string?", defaultValue = "comma")
    val argumentSeparator: CwtLinkArgumentSeparator
    @FromProperty("prefix: string?")
    val prefix: String?
    @FromProperty("data_source: string?", multiple = true)
    val dataSources: List<String>
    @FromProperty("input_scopes: string[]")
    val inputScopes: Set<String>
    @FromProperty("output_scope: string?")
    val outputScope: String?
    @FromProperty("for_definition_type: string?")
    val forDefinitionType: String?

    val isLocalisationLink: Boolean
    val dataSourceIndex: Int
    val dataSourceExpression: CwtDataExpression?
    val dataSourceExpressions: List<CwtDataExpression>
    override val configExpression: CwtDataExpression?

    interface Resolver {
        /** 由属性规则解析为（常规）链接规则。 */
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig?
        /** 由属性规则解析为本地化链接规则。 */
        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig?
        /** 由已有的（常规）链接规则，解析为本地化链接规则。 */
        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig
        /** 构造一个委托版本（wrapper），并指定数据源的索引。如果索引越界，则返回 null。 */
        fun delegatedWith(linkConfig: CwtLinkConfig, dataSourceIndex: Int): CwtLinkConfig?
    }

    companion object : Resolver by CwtLinkConfigResolverImpl()
}

