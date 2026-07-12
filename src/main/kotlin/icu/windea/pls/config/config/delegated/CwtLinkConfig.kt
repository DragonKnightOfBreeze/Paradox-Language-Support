package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtLinkArgumentSeparator
import icu.windea.pls.config.config.CwtLinkType
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeConstants


/**
 * 链接规则。
 *
 * 用于描述各种链式表达式中的链接节点的格式，并为其指定允许的数据源类型和作用域类型。
 * 这些链接节点通常用于切换作用域或取值，可以链式调用（如 `x.y.z`），并且可能带有数据源（如 `modifier:x` 和 `relations(x)`）。
 * 在语义与格式上，它们类似编程语言中的函数、属性或字段。
 *
 * 链接规则驱动了这些复杂表达式的解析逻辑以及各种相关的语言功能。
 *
 * 链接可以以静态（静态链接）或动态（动态链接）两种方式声明。具体而言，动态链接可以：
 * - 整个文本作为动态数据（如 `var`）。
 * - 带前缀（如 `modifier:x`）。
 * - 使用函数调用形式（如 `relations(x)`）。
 * - 使用更复杂的函数调用形式（如，使用嵌套的链式表达式作为参数）。注意，插件尚未提供完整支持。
 *
 * 以下是一些常见的链接形式：
 * - **作用域链接（scope link）** - 如 `owner`。
 * - **系统作用域（system scope）** - 如 `root`。
 * - **本地化命令字段（localisation command field）** - 如 `GetName`。
 * - **事件对象引用（event target reference）** - 如 `event_target:x`。
 * - **动态数据引用（dynamic data reference）** - 如 `modifier:x` `value:x`。
 *
 * 路径定位：
 * - 常规链接：`links/{name}`。其中 `{name}` 匹配规则名称。
 * - 本地化链接：`localisation_links/{name}`。其中 `{name}` 匹配规则名称。
 * - 如果静态的本地化链接未被声明，静态的常规链接会被全部复制作为本地化链接。
 *
 * 示例：
 *
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
 * > CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。
 *
 * @property name 规则名称（即链接名）。
 * @property type 链接类型（`scope`/`value`/`both`，默认为 `scope`）。
 * @property fromData 为动态链接时，是否从后置数据中读取动态数据。对应的节点格式形如 `prefix:data`。
 * @property fromArgument （扩展）为动态链接时，是否从传参中读取动态数据。对应的节点格式形如 `func(arg)`。
 * @property argumentSeparator （扩展） 为动态链接且有多个传参时，使用的传参分隔符（`comma`/`pipe`，默认为 `comma`）。
 * @property prefix 为动态链接时，携带的前缀。如果为 `null`，则将整个文本作为动态数据。
 * @property dataSources 数据源（数据表达式）。如果有多个，则意味着此链接为动态链接且有多个传参。如果为空，则意味着此链接为静态链接。
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
interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName
    val name: String
    @FromMember("type: string?", allowedValues = ["scope", "value", "both"], defaultValue = "scope")
    val type: CwtLinkType
    @FromMember("from_data: boolean", defaultValue = "no")
    val fromData: Boolean
    @FromMember("from_argument: boolean", defaultValue = "no")
    val fromArgument: Boolean
    @FromMember("argument_separator: string?", allowedValues = ["comma", "pipe"], defaultValue = "comma")
    val argumentSeparator: CwtLinkArgumentSeparator
    @FromMember("prefix: string?")
    val prefix: String?
    @FromMember("data_source: string?", multiple = true)
    val dataSources: List<String>
    @FromMember("input_scopes: string[]")
    val inputScopes: Set<String>
    @FromMember("output_scope: string?")
    val outputScope: String?
    @FromMember("for_definition_type: string?")
    val forDefinitionType: String?

    val isLocalisationLink: Boolean
    val dataSourceIndex: Int
    val dataSourceExpression: CwtDataExpression?
    val dataSourceExpressions: List<CwtDataExpression>
    override val configExpression: CwtDataExpression?

    companion object {
        /** 由属性规则解析为（常规）链接规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig? {
            return CwtLinkConfigResolver.resolve(config)
        }

        /** 由属性规则解析为本地化链接规则。 */
        @JvmStatic
        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig? {
            return CwtLinkConfigResolver.resolveForLocalisation(config)
        }

        /** 由已有的（常规）链接规则，解析为本地化链接规则。 */
        @JvmStatic
        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig {
            return CwtLinkConfigResolver.resolveForLocalisation(linkConfig)
        }

        /** 构造一个委托版本（wrapper），并指定数据源的索引。如果索引越界，则返回 null。 */
        @JvmStatic
        fun delegatedWith(linkConfig: CwtLinkConfig, dataSourceIndex: Int): CwtLinkConfig? {
            return CwtLinkConfigResolver.delegatedWith(linkConfig, dataSourceIndex)
        }
    }
}

// region Implementations

private object CwtLinkConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config, false)

    fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config, true)

    fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig = doResolve(linkConfig, true)

    private fun doResolve(config: CwtPropertyConfig, isLocalisationLink: Boolean): CwtLinkConfig? {
        val name = config.key
        val propConfigs = config.properties
        if (propConfigs == null) {
            logger.warn("Skipped invalid link config (name: $name): Null properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propConfigs.groupBy { it.key }
        val type = propGroup.getOne("type")?.stringValue.let { CwtLinkType.resolve(it) }
        val fromData = propGroup.getOne("from_data")?.booleanValue ?: false
        val fromArgument = propGroup.getOne("from_argument")?.booleanValue ?: false
        val argumentSeparator = propGroup.getOne("argument_separator")?.stringValue.let { CwtLinkArgumentSeparator.resolve(it) }
        val prefix = propGroup.getOne("prefix")?.stringValue?.orNull()
        val dataSources = propGroup.getAll("data_source").mapNotNull { it.stringValue }.optimized()
        val inputScopes = buildSet {
            // both input_scopes and input_scope are supported
            propGroup.getAll("input_scopes").forEach { p ->
                p.stringValue?.let { v -> add(ParadoxScope.getId(v)) }
                p.values?.forEach { it.stringValue?.let { v -> add(ParadoxScope.getId(v)) } }
            }
            propGroup.getAll("input_scope").forEach { p ->
                p.stringValue?.let { v -> add(ParadoxScope.getId(v)) }
                p.values?.forEach { it.stringValue?.let { v -> add(ParadoxScope.getId(v)) } }
            }
        }.optimized().orNull() ?: ParadoxScopeConstants.anyScopes
        val outputScope = propGroup.getOne("output_scope")?.stringValue?.let { v -> ParadoxScope.getId(v) }
        val forDefinitionType = propGroup.getOne("for_definition_type")?.stringValue

        // when from data or from argument, data sources must not be empty
        if (fromData && dataSources.isEmpty()) {
            logger.warn("Skipped invalid link config (name: $name): No data_source properties while from_data = yes.".withLocationPrefix(config))
            return null
        }
        if (fromArgument && dataSources.isEmpty()) {
            logger.warn("Skipped invalid link config (name: $name): No data_source properties while from_argument = yes.".withLocationPrefix(config))
            return null
        }

        logger.debug { "Resolved link config (name: $name).".withLocationPrefix(config) }
        return CwtLinkConfigImpl(
            config, name, type, fromData, fromArgument, argumentSeparator,
            prefix, dataSources, inputScopes, outputScope,
            forDefinitionType, isLocalisationLink
        )
    }

    @Suppress("SameParameterValue")
    private fun doResolve(linkConfig: CwtLinkConfig, isLocalisationLink: Boolean): CwtLinkConfig {
        return linkConfig.apply {
            CwtLinkConfigImpl(
                config, name, type, fromData, fromArgument, argumentSeparator,
                prefix, dataSources, inputScopes, outputScope,
                forDefinitionType, isLocalisationLink
            )
        }
    }

    fun delegatedWith(linkConfig: CwtLinkConfig, dataSourceIndex: Int): CwtLinkConfig? {
        if (dataSourceIndex < 0 || dataSourceIndex > linkConfig.dataSources.lastIndex) return null
        if (dataSourceIndex == linkConfig.dataSourceIndex) return linkConfig
        if (linkConfig.dataSources.size <= 1) return linkConfig
        return CwtLinkConfigDelegate(linkConfig, dataSourceIndex)
    }
}

private class CwtLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: CwtLinkType,
    override val fromData: Boolean,
    override val fromArgument: Boolean,
    override val argumentSeparator: CwtLinkArgumentSeparator,
    override val prefix: String?,
    override val dataSources: List<String>,
    override val inputScopes: Set<String>,
    override val outputScope: String?,
    override val forDefinitionType: String?,
    override val isLocalisationLink: Boolean,
) : UserDataHolderBase(), CwtLinkConfig {
    override val dataSourceIndex: Int get() = 0
    override val dataSourceExpressions = dataSources.map { CwtDataExpression.resolve(it, false) }.optimized()
    override val dataSourceExpression = dataSourceExpressions.getOrNull(dataSourceIndex) ?: dataSourceExpressions.firstOrNull()
    override val configExpression: CwtDataExpression? get() = dataSourceExpression

    override fun toString() = "CwtLinkConfigImpl(name='$name')"
}

private class CwtLinkConfigDelegate(
    val delegate: CwtLinkConfig,
    override val dataSourceIndex: Int
) : CwtLinkConfig by delegate {
    // NOTE 需要重载下面两个属性
    override val dataSourceExpression = dataSourceExpressions.getOrNull(dataSourceIndex) ?: dataSourceExpressions.firstOrNull()
    override val configExpression get() = dataSourceExpression

    override fun toString() = "CwtLinkConfigDelegate(name='$name', dataSourceIndex='$dataSourceIndex')"
}

// endregion
